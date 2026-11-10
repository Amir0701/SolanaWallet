# SolanaCryproWallet

Android-кошелёк для одного Solana-аккаунта. Пользователь создаёт или импортирует BIP39-фразу, смотрит баланс в SOL, копирует адрес, отправляет SOL и переключает Devnet и Mainnet. Приложение однопользовательское: на устройстве хранится одна мнемоника.

Имя Gradle-проекта — `SolanaCryproWallet`, `applicationId` и пакет приложения — `com.sol.cryprowallet`. Опечатка `crypro` зафиксирована в идентификаторах.

## Стек

| | |
|---|---|
| Язык | Kotlin, JVM 11 |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| DI | Hilt в приложении и фичах, AssistedInject в сети |
| Сеть | solanaj (`com.github.skynetcap:solanaj`) через JitPack |
| Мнемоника | bitcoinj `MnemonicCode` (генерация и проверка BIP39) |
| Хранение | `EncryptedSharedPreferences`, мастер-ключ AES256-GCM в Android Keystore |
| Тесты | JUnit 4, MockK, Turbine, Compose UI Test, Compose Screenshot |
| Сборка | AGP 8.13.2, Gradle 8.13, Kotlin 2.2.21, compileSdk / targetSdk 36, minSdk 28 |

Версии объявлены в `gradle/libs.versions.toml`. Репозитории: Google, Maven Central, JitPack. `RepositoriesMode.FAIL_ON_PROJECT_REPOS` — репозиторий в модуле добавлять нельзя, только в `settings.gradle.kts`.

Плагин `org.jetbrains.kotlin.android` в каталоге зафиксирован на `2.2.0`, тогда как `kotlin` и KSP — на `2.2.21`. Меняй это только осознанно, вместе с KSP.

## Модули

```
:app
 ├── :feature:auth:api / :impl
 ├── :feature:send:api / :impl
 ├── :feature:receive:api / :impl
 └── :core:wallet
      ├── :core:crypto
      └── :core:network
```

| Модуль | Пакет | Ответственность |
|---|---|---|
| `:app` | `com.sol.cryprowallet` | `Application`, `MainActivity`, тема, `AppModule`, хост навигации, домашний экран |
| `:core:crypto` | `com.sol.crypto` | Мнемоника, деривация аккаунта, шифрованное хранение |
| `:core:network` | `com.sol.network` | RPC-клиент: баланс и перевод SOL |
| `:core:wallet` | `com.sol.core.wallet` | Сессия, выбор кластера, форматирование lamports |
| `:feature:*:api` | `com.sol.feature.<name>.api` | Маршрут и функция навигации |
| `:feature:*:impl` | `com.sol.feature.<name>.impl` | Экран, ViewModel, регистрация destination |

`:app` зависит и от `api`, и от `impl`, потому что собирает граф и создаёт `SecureStorage`. Фича видит только `:core:wallet` и свой `api`. Так экран отправки не знает, как устроен RPC и где лежит сид.

Целевое устройство кода внутри `impl` (слои domain / data / presentation) и статус перевода фич описаны в `docs/architecture.md`.

## Поток приложения

`SolanaCryproWalletApplication` помечен `@HiltAndroidApp`. `MainActivity` включает edge-to-edge и рисует `WalletApp` внутри `SolanaCryproWalletTheme`.

`WalletApp` читает сессию из `WalletAppViewModel`. Стартовый маршрут запоминается один раз: `home`, если кошелёк уже есть, иначе `auth`. Верхняя панель показывает переключатель «Сеть: Devnet» / «Сеть: Mainnet», когда кошелёк создан. Сообщения фич приходят колбэком `onShowMessage` и уходят в один `SnackbarHost`.

Маршруты:

| Маршрут | Где объявлен | Экран |
|---|---|---|
| `auth` | `AuthFeature.ROUTE` | Создание или импорт |
| `home` | `HOME_ROUTE` в `:app` | Баланс, адрес, переход к send/receive, удаление |
| `send` | `SendFeature.ROUTE` | Получатель и сумма в SOL |
| `receive` | `ReceiveFeature.ROUTE` | Адрес и копирование |

После создания или импорта `auth` снимается со стека, остаётся `home`. Удаление кошелька очищает сессию и возвращает на `auth`, очищая стек до стартового destination.

## Сессия

`WalletSessionManager` — singleton и единственная точка, через которую фичи трогают ключи и RPC.

```kotlin
data class WalletSession(
    val hasWallet: Boolean = false,
    val publicKey: String? = null,
    val useDevnet: Boolean = true,
)
```

Операции:

- `createWallet()` — новая фраза из 12 слов, сохранение, публикация адреса.
- `importWallet(words)` — проверка BIP39, сохранение. Результат: `ImportResult.Success`, `InvalidMnemonic` или `Error`.
- `deleteWallet()` — стирает мнемонику и сбрасывает `hasWallet` / `publicKey`. Выбранная сеть при этом не сбрасывается.
- `toggleNetwork()` — переключает Devnet и Mainnet. `HomeViewModel` следит за парой `publicKey` + `useDevnet` и заново запрашивает баланс.
- `getBalanceLamports()` — баланс текущего адреса в текущем кластере.
- `transfer(recipient, lamports)` — подпись и отправка. Signer наружу не отдаётся.

Кластеры в `SolanaCluster`:

- Devnet: `https://api.devnet.solana.com`
- Mainnet: `https://api.mainnet-beta.solana.com`

`SolanaRepository` создаётся фабрикой AssistedInject с URL выбранного кластера. Клиент — `RpcClient` из solanaj. Баланс: `getBalance`. Перевод: одна инструкция `SystemProgram.transfer`, затем `sendTransaction`. Возвращается сигнатура транзакции.

## Ключи

`WalletManager` не помечен `@Inject`. Его собирает `AppModule` из `SecureStorage`.

Создание: 16 байт из `SecureRandom` → BIP39 через `MnemonicCode` → сохранение → аккаунт.

Импорт: `MnemonicCode.check`. Чужое исключение bitcoinj снаружи не торчит, вместо него `InvalidMnemonicException`.

Деривация: `Account.fromBip44MnemonicWithChange(words, "")` в solanaj, passphrase пустая. Публичный ключ — Base58. В `Wallet` дополнительно пишется `createdAt`, но между запусками адрес каждый раз считается из мнемоники заново, отдельно он не хранится.

`SecureStorage`:

- файл префов: `secure_wallet_storage`
- ключ: `mnemonic`
- значение: слова через пробел
- шифрование ключей AES256-SIV, значений AES256-GCM, мастер-ключ Android Keystore

Подпись транзакции использует тот же путь: `getSignerAccount()` заново читает фразу и строит `Account`.

## Фичи

**Auth.** `AuthViewModel` держит текст импорта и, после создания, список слов. Диалог просит подтвердить, что фраза сохранена; только потом уходит событие `authenticated`. Ошибки — `AuthError.InvalidMnemonic` или `AuthError.Plain`, экран переводит их в snackbar и сбрасывает.

**Home.** Экран в `:app`, не в отдельной фиче: `HomeViewModel` и `HomeScreen`. Показывает баланс через `SolFormatting.formatLamportsAsSol`, адрес, кнопки «Отправить», «Получить», копирование адреса и удаление кошелька. Пока баланс грузится, вместо суммы индикатор. Ошибка RPC оставляет баланс `null` (на экране «—»).

**Send.** Поля получателя и суммы. Сумма парсится `SolFormatting.parseSolToLamports` (запятая или точка, обрезка дробной части lamport вниз, пустая строка и нечисло — ошибка). Нулевая и отрицательная сумма не отправляются. Успех очищает поля и кладёт сигнатуру в `lastSignature`.

**Receive.** Подписка на `publicKey` из сессии (`SharingStarted.WhileSubscribed(5_000)`). Копирование в буфер и snackbar.

`SolFormatting` — чистые функции без Android. 1 SOL = 1_000_000_000 lamports. Округление вниз.

## DI

| Класс | Как попадает в граф |
|---|---|
| `SecureStorage`, `WalletManager` | `@Provides` в `AppModule`, `SingletonComponent` |
| `WalletSessionManager` | `@Inject` конструктор, `@Singleton` |
| `SolanaRepository` | `@AssistedInject`, фабрика `SolanaRepository.Factory` |
| ViewModel-ы | `@HiltViewModel`, в Composable — `hiltViewModel()` |

Фичевые `impl` подключают плагины Hilt, KSP и Compose. Модули `api` — только Android library и Navigation Compose, без Hilt и без Compose UI.

## UI

- Строки домашнего экрана — `app/src/main/res/values/strings.xml`.
- Строки auth / send / receive — в `strings.xml` соответствующего `impl`.
- Цвета и типографика — `ui/theme` в `:app`. Dynamic color включён на Android 12+.
- Экраны фич — `internal`, кроме содержимого, которое нужно screenshot-тестам (`SendScreenContent` публичный).
- Отступы хоста задаёт `WalletNavHost`: padding от `Scaffold` плюс 16 dp.

Разрешение в манифесте одно: `INTERNET`.

## Сборка и проверка

```bash
./gradlew :app:assembleDebug
./gradlew :core:crypto:test :core:wallet:test :core:network:test
./gradlew :app:testDebugUnitTest
./gradlew :feature:auth:impl:test :feature:send:impl:test :feature:receive:impl:test
```

На устройстве или эмуляторе:

```bash
./gradlew :app:connectedDebugAndroidTest
./gradlew :feature:auth:impl:connectedDebugAndroidTest
./gradlew :feature:send:impl:connectedDebugAndroidTest
./gradlew :feature:receive:impl:connectedDebugAndroidTest
```

Скриншот-тесты включены флагом `android.experimental.enableScreenshotTest` в модулях с UI (`:app` и три `impl`). Эталоны обновляет и проверяет плагин `com.android.compose.screenshot` (`update*ScreenshotTest` / `validate*ScreenshotTest` для debug).

Юнит-тесты ViewModel используют общий приём: `MainDispatcherRule` подменяет `Dispatchers.Main`, зависимости мокаются MockK, `StateFlow` читается через Turbine.

## Ограничения текущей версии

- Один кошелёк на установку. Повторное создание перезаписывает мнемонику.
- Только нативный SOL. SPL-токены, история, комиссия отдельной настройкой, приоритетные fee и свой RPC не реализованы.
- Пароль BIP39 всегда пустой.
- Сеть не сохраняется между процессами отдельно от значения по умолчанию: при старте `useDevnet` снова `true`, адрес поднимается из хранилища.
- Резервные правила в `res/xml` — заготовки Android Studio и не исключают `secure_wallet_storage` явно. Хранилище зашифровано, но облачный бэкап на него расширять нельзя.
- ProGuard для release выключен (`isMinifyEnabled = false`).
