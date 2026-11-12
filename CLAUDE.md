# SolanaCryproWallet

Android-кошелёк для одного Solana-аккаунта: создание и импорт BIP39-фразы, баланс, отправка и получение SOL, переключение Devnet / Mainnet. Kotlin, Jetpack Compose, Hilt, многомодульный Gradle-проект.

## Сборка и тесты

```bash
./gradlew :app:assembleDebug                              # сборка
./gradlew :feature:send:impl:test                         # unit-тесты одного модуля
./gradlew testDebugUnitTest                               # все unit-тесты, как в CI
./gradlew :feature:send:impl:validateDebugScreenshotTest  # скриншоты одного модуля
./gradlew :feature:send:impl:connectedDebugAndroidTest    # Compose-тесты, нужен эмулятор
```

## Ключевые файлы

- Навигация: `app/src/main/java/com/sol/cryprowallet/wallet/WalletNavHost.kt`
- Сессия — единственный вход фич к ключам и RPC: `core/wallet/src/main/java/com/sol/core/wallet/WalletSessionManager.kt`
- Ключи и хранилище: `core/crypto/src/main/java/com/sol/crypto/WalletManager.kt`, `SecureStorage.kt`
- RPC: `core/network/src/main/java/com/sol/network/SolanaRepository.kt`
- DI на уровне приложения: `app/src/main/java/com/sol/cryprowallet/di/AppModule.kt`
- Версии библиотек: `gradle/libs.versions.toml`

## Архитектура

- Фича `:feature:<name>:impl` зависит только от `:core:wallet` и своего `:api`. Не от `:core:crypto`, `:core:network` и не от чужого `impl`. Общее состояние — `WalletSessionManager.session`.
- Внутри `impl` код делится на слои domain / data / presentation. Правила слоёв и статус перевода фич — `docs/architecture.md`.
- UI только на Compose и Material 3. Состояние экрана живёт во ViewModel как `StateFlow<UiState>` и собирается через `collectAsStateWithLifecycle()`. Одноразовые события — `Channel` + `receiveAsFlow()`.
- Экран не получает `NavController`: навигация и сообщения уходят колбэками, которые подключает `WalletNavHost`.
- Суммы в коде — `Long` в lamports (1 SOL = 10⁹). Переводи SOL ↔ lamports только через `SolFormatting`.
- Устройство модулей, сессии и DI подробно — `project.md`.

## Зависимости

Используй библиотеки и версии из `gradle/libs.versions.toml`. Перед добавлением новой зависимости спроси: в кошельке любая библиотека работает в одном процессе с ключами.

## Соглашения

- Тексты для пользователя — в `strings.xml` своего модуля, на русском. Комментарии — по-русски и только там, где намерение неочевидно.
- Не переименовывай идентификаторы с опечаткой `crypro`: это applicationId и пакеты.
- Минимальный diff: не рефактори то, чего задача не касается.
- Без прямой просьбы не добавляй SPL-токены, историю транзакций, несколько кошельков, биометрию, свой RPC, swap и восстановление из бэкапа.

## Безопасность

Это кошелёк: ошибка в ключе или сети означает потерю средств.

- Мнемоника живёт только в `SecureStorage` (`secure_wallet_storage`). Её нет в логах, обычных prefs, тестах, скриншотах и текстах ошибок. В UI она показывается один раз, после создания, и сразу убирается из состояния.
- Не меняй деривацию в `WalletManager` (`fromBip44MnemonicWithChange`, пустой passphrase) и формат хранения фразы без прямой просьбы: иначе старый кошелёк получит другой адрес.
- Приватный ключ не покидает `WalletSessionManager.transfer`.
- По умолчанию Devnet. Ничего не запускай против Mainnet или реального RPC и не подписывай транзакции. В тестах — моки и публичный тестовый вектор BIP39.
- Не добавляй `secure_wallet_storage` в `backup_rules.xml` и `data_extraction_rules.xml`.

## Когда задача готова

1. `:app:assembleDebug` проходит, unit-тесты затронутых модулей зелёные. После правки `:core:*` — ещё и тесты зависимых модулей.
2. Менялся UI — `validateDebugScreenshotTest` затронутых модулей. Есть эмулятор — их `connectedDebugAndroidTest`.
3. Менялся код фичи на слоях — проверки из «Как проверяется» в `docs/architecture.md` дают пустой вывод.
4. Закоммить в отдельную ветку от `master` (в сам `master` — никогда), запушь и создай PR в `master` через `gh pr create`. Если PR для ветки уже открыт — только запушь. В коммит не попадают `.idea/`, `local.properties` и изменения, не относящиеся к задаче.
5. Дождись CI: `gh pr checks <номер> --watch` в фоне. Упала проверка — смотри `gh run view <id> --log-failed`, чини и пушь снова.
6. В ответе — какие команды запускал, с каким результатом, что не запускал, ссылка на PR и статус каждой проверки CI.

Задача на несколько модулей или любой `:core:*` — сначала план в plan mode.

## Не ослабляй проверки

- Не удаляй тесты, не ставь `@Ignore`, не подгоняй ожидания. Падение — повод чинить код или согласовать новое поведение.
- Эталоны скриншотов (`update*ScreenshotTest`) обновляй только по просьбе.
- Без просьбы не трогай `.github/workflows/`, `local.properties`, `.idea/`.

## Skills

- `new-feature` — новая фича: модули, слои, навигация, тесты.
- `add-use-case` — новая операция в существующей фиче.
- `migrate-feature` — перевод существующей фичи на слои.
