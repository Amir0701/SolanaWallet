# Архитектура фич

Целевая архитектура кода внутри `:feature:<name>:impl`. Обязательна для новых фич. Существующие фичи ещё не переведены, см. «Статус миграции».

Модульная схема не меняется. Границы между модулями (`api` / `impl`, зависимость только от `:core:wallet`, общее состояние в `WalletSessionManager`) описаны в `CLAUDE.md` и `project.md`. Этот документ отвечает на вопрос, как разложить код внутри одного `impl`.

## Слои и правило зависимостей

```
presentation ──→ domain ←── data ──→ WalletSessionManager (:core:wallet)
```

Зависимости направлены к `domain`. `domain` не знает ни о `presentation`, ни о `data`, ни об Android.

| Слой | Пакет | Что внутри | Может импортировать | Не может импортировать |
|---|---|---|---|---|
| domain | `<impl>.domain` | модели, интерфейсы репозиториев, use case | Kotlin stdlib, `java.math`, `kotlinx.coroutines` (кроме `Dispatchers`), `javax.inject`, `com.sol.core.wallet.SolFormatting` | `android.*`, `androidx.*`, `dagger.hilt.*`, остальное из `com.sol.core.wallet`, `data`, `presentation` |
| data | `<impl>.data` | реализации репозиториев | `domain`, `WalletSessionManager` и типы `:core:wallet`, `kotlinx.coroutines` | `presentation` |
| presentation | `<impl>.presentation` | ViewModel, экран, сообщения | `domain` (use case и модели), Compose, Lifecycle, Hilt | `data`, `WalletSessionManager`, интерфейсы репозиториев, `Dispatchers` |
| di | `<impl>.di` | Hilt-модуль с `@Binds` | `domain`, `data` | `presentation` |

`<impl>` — `com.sol.feature.<name>.impl`.

## Структура фичи

```
feature/<name>/impl/src/
├── main/java/com/sol/feature/<name>/impl/
│   ├── <Name>NavGraph.kt               NavGraphBuilder.<name>Screen() — единственный вход для :app
│   ├── domain/
│   │   ├── model/                      <Action>Result.kt, доменные модели
│   │   ├── repository/<X>Repository.kt
│   │   └── usecase/<Verb><Noun>UseCase.kt
│   ├── data/<X>RepositoryImpl.kt
│   ├── di/<Name>Module.kt
│   └── presentation/
│       ├── <Name>ViewModel.kt          UiState внутри, sealed-сообщения рядом
│       └── <Name>Screen.kt             <Name>Screen (internal) + <Name>ScreenContent
├── main/res/values/strings.xml
├── test/java/com/sol/feature/<name>/impl/
│   ├── MainDispatcherRule.kt
│   ├── domain/usecase/<Verb><Noun>UseCaseTest.kt
│   ├── data/<X>RepositoryImplTest.kt
│   └── presentation/<Name>ViewModelTest.kt
├── screenshotTest/…/<Name>ScreenScreenshotTest.kt
└── androidTest/…/<Name>ScreenTest.kt
```

`<Name>NavGraph.kt` остаётся в корневом пакете `impl`, чтобы импорт в `WalletNavHost` не зависел от внутренней раскладки.

Код каждого слоя на примере send — `.claude/skills/new-feature/templates.md`.

## domain

- Use case выполняет одну операцию через `operator fun invoke`. Разовая операция — `suspend`, наблюдение — `Flow` без `suspend`.
- Имя: `<Глагол><Объект>UseCase`. Наблюдение начинается с `Observe`: `SendSolUseCase`, `ImportWalletUseCase`, `ObservePublicKeyUseCase`.
- Конструктор помечен `@Inject` и принимает только интерфейсы репозиториев своей фичи. Use case не вызывает другие use case.
- Бизнес-правила живут здесь: разбор и проверка суммы (`SolFormatting.parseSolToLamports`, сумма `> 0`), нормализация адреса и слов мнемоники. Во ViewModel и в репозитории их нет.
- Ожидаемые исходы описывает sealed-тип `<Action>Result`, например `SendSolResult.Success(signature)`, `InvalidAmount`, `Failed(reason)`. Use case не бросает исключения для ожидаемых ошибок и не возвращает `kotlin.Result`.
- Модели — `data class` без Android-типов. Сумма — `Long` в lamports, адрес — `String` в Base58.
- Интерфейс репозитория описывает то, что нужно фиче, а не RPC. Он main-safe: вызывающий не переключает поток. Метод возвращает `Result<T>`, если ошибка одна, или доменный sealed-тип, если вид ошибки важен (импорт: неверная фраза или другая ошибка).

## data

- `<X>RepositoryImpl @Inject constructor(private val sessionManager: WalletSessionManager) : <X>Repository`.
- Это тонкий адаптер: вызывает `WalletSessionManager` и переводит `Result`, `ImportResult`, `WalletSession` в доменные типы. Бизнес-правил здесь нет.
- Поток переключает репозиторий: `withContext(Dispatchers.IO)` для RPC (баланс, перевод), `withContext(Dispatchers.Default)` для мнемоники (создание, импорт).
- Состояния у репозитория нет, полей кроме зависимостей нет. Состояние сессии — `sessionManager.session`, наружу через `Flow` с `map`.
- Не оборачивай `suspend`-вызовы в `runCatching`: он глотает `CancellationException`.

## presentation

- ViewModel зависит только от use case. Она не импортирует `data`, `WalletSessionManager`, репозитории и `Dispatchers`: use case и репозитории уже main-safe.
- `UiState` — `data class` внутри ViewModel, наружу `StateFlow`. Одноразовые события — `Channel` + `receiveAsFlow()`, тип событий — sealed interface в том же файле.
- ViewModel переводит `<Action>Result` в `UiState` и события. Текст для пользователя берётся из `strings.xml` в Composable, а не во ViewModel.
- `<Name>Screen` — `internal`, берёт ViewModel через `hiltViewModel()`, собирает состояние через `collectAsStateWithLifecycle()` и передаёт его в `<Name>ScreenContent`.
- `<Name>ScreenContent` не хранит состояние: получает `UiState` и лямбды. Лямбды передают новое значение: `onAmountChange: (String) -> Unit`, а не `() -> Unit`.
- Экран не знает о `NavController`. Навигация и сообщения уходят колбэками (`onBack`, `onShowMessage`), их подключает `WalletNavHost`.
- Видимость как сейчас: `<Name>Screen` — `internal`, `<Name>ScreenContent` и ViewModel с `UiState` — public, потому что их видят скриншот-тесты. Use case и доменные модели тогда тоже public: public-конструктор ViewModel не может принимать internal-тип. `:app` импортирует из `impl` только `<name>Screen()`.

## di

```kotlin
@Module
@InstallIn(ViewModelComponent::class)
abstract class SendModule {
    @Binds
    abstract fun bindTransferRepository(impl: TransferRepositoryImpl): TransferRepository
}
```

Use case и реализации репозиториев создаются через `@Inject`-конструктор, без `@Provides`. Scope-аннотаций нет: всё долгоживущее состояние в `WalletSessionManager`.

## Безопасность

- Ключи и подпись остаются в `WalletSessionManager`. Ни один слой фичи не получает `Account`, signer или приватный ключ, и в интерфейсах репозиториев нет методов, которые их возвращают.
- Мнемоника идёт одним путём: результат `CreateWalletUseCase` → `UiState` → диалог → очистка после подтверждения. Не храни её в полях use case и репозитория, не логируй `UiState` auth, не вставляй слова в `reason` и тексты ошибок.
- Фича не выбирает кластер. Devnet или Mainnet определяет сессия.

## Тесты по слоям

| Слой | Что проверять | Двойники | Особенности |
|---|---|---|---|
| domain | каждую ветку `<Action>Result`, правила валидации | MockK интерфейса репозитория, `coEvery` / `coVerify` | `runTest`, без `MainDispatcherRule` |
| data | перевод типов сессии в доменные, вызов нужного метода сессии | `mockk<WalletSessionManager>()` | `runTest`, без реального RPC |
| presentation | `UiState` и события для каждого исхода use case | MockK use case | `MainDispatcherRule`, Turbine |
| UI | `<Name>ScreenContent` | фейковый `UiState` и фейковый адрес, как в существующих скриншот-тестах | скриншот-тест и Compose-тест в `androidTest` |

Правила валидации проверяются в тестах use case, а не ViewModel.

## Как вносить изменения

| Задача | Skill |
|---|---|
| Новая фича (новый экран и модули) | `new-feature` |
| Новая операция в переведённой фиче | `add-use-case` |
| Перевод существующей фичи на слои | `migrate-feature` |

## Статус миграции

| Фича | Статус |
|---|---|
| `send` | не переведена, станет эталоном |
| `receive` | не переведена |
| `auth` | не переведена |
| home в `:app` | не фича-модуль, вне этого документа |

Не переводи фичу попутно с другой задачей. Перевод делается по просьбе, целиком: все слои и тесты за один раз. После перевода обнови эту таблицу. Когда send переведена, `templates.md` сверяется с её кодом, а при расхождении прав `templates.md`.

## Как проверяется

Пока правила держатся на ревью и на трёх командах. В переведённых фичах вывод должен быть пустым:

```bash
# domain без Android и Hilt
grep -rnE '^import (android|androidx|dagger\.hilt)\.' --include='*.kt' feature | grep '/impl/domain/'
# domain берёт из :core:wallet только SolFormatting
grep -rnE '^import com\.sol\.core\.wallet\.' --include='*.kt' feature | grep '/impl/domain/' | grep -v SolFormatting
# presentation без сессии, data и Dispatchers
grep -rnE '^import (com\.sol\.core\.wallet\.WalletSessionManager|com\.sol\.feature\.[a-z]+\.impl\.data\.|kotlinx\.coroutines\.Dispatchers)' --include='*.kt' feature | grep '/impl/presentation/'
```

План: заменить эти команды Konsist-тестами, чтобы нарушение роняло `testDebugUnitTest`.
