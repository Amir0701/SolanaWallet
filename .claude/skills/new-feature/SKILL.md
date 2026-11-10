---
name: new-feature
description: Создаёт новую фичу SolanaCryproWallet — модули :feature:<name>:api и :impl со слоями domain/data/presentation, подключение в WalletNavHost, строки и тесты. Используй, когда просят новый экран, новую фичу или новый feature-модуль (new feature, new screen).
---

# Новая фича

Правила слоёв — `docs/architecture.md`. Код каждого слоя — `templates.md` рядом с этим файлом. Прочитай оба перед началом.

## 1. Уточни до кода

- Имя фичи (`<name>`, латиница, одно слово), маршрут, что делает экран, откуда на него попадают и куда он ведёт.
- Какие операции `WalletSessionManager` нужны. Если нужной операции нет, остановись и согласуй с пользователем: это изменение `:core:wallet`, а иногда и ключей или сети.
- Если фича из списка «Чего в приложении нет» в `CLAUDE.md`, убедись, что пользователь просит её прямо.

Составь план в plan mode: файлы по слоям, use case и их `<Action>Result`, тесты.

## 2. Модули

1. `settings.gradle.kts`: `include(":feature:<name>:api")` и `include(":feature:<name>:impl")`.
2. `api`: скопируй `feature/send/api/build.gradle.kts` и `AndroidManifest.xml`, замени `namespace` на `com.sol.feature.<name>.api`. Создай `<Name>Navigation.kt` по образцу `SendNavigation.kt`: `object <Name>Feature { const val ROUTE = "<name>" }` и `NavController.navigateTo<Name>()`.
3. `impl`: скопируй `feature/send/impl/build.gradle.kts` и `AndroidManifest.xml`, замени `namespace` на `com.sol.feature.<name>.impl` и зависимость `:feature:send:api` на `:feature:<name>:api`. Других модулей проекта, кроме `:core:wallet`, не добавляй.
4. `app/build.gradle.kts`: `implementation(project(":feature:<name>:api"))` и `implementation(project(":feature:<name>:impl"))` рядом с другими фичами.

## 3. Код снизу вверх

1. `domain/model`: `<Action>Result` и модели.
2. `domain/repository`: интерфейсы.
3. `domain/usecase`: use case с бизнес-правилами.
4. `data`: `<X>RepositoryImpl` поверх `WalletSessionManager`, переключение потока.
5. `di/<Name>Module.kt`: `@Binds` для каждого репозитория.
6. `presentation`: ViewModel и `<Name>Screen.kt` с `<Name>Screen` и `<Name>ScreenContent`.
7. `<Name>NavGraph.kt` в корневом пакете: `NavGraphBuilder.<name>Screen(...)`.
8. `res/values/strings.xml`: строки на русском с префиксом `<name>_`.
9. `WalletNavHost`: вызов `<name>Screen(...)` и переход на экран. Если переход — кнопка на home, её строка идёт в `app/src/main/res/values/strings.xml`.

## 4. Тесты

- `test`: тест на каждый use case (все ветки `<Action>Result`), на каждый репозиторий и на ViewModel. `MainDispatcherRule` скопируй из `feature/send/impl/src/test`.
- `screenshotTest`: `<Name>ScreenScreenshotTest` для `<Name>ScreenContent` с фейковым адресом, без мнемоники.
- `androidTest`: `<Name>ScreenTest` для основных действий на `<Name>ScreenContent`.
- Эталоны скриншотов для нового модуля создай через `./gradlew :feature:<name>:impl:updateDebugScreenshotTest` и сообщи об этом в ответе. Эталоны других модулей не трогай.

## 5. Проверка

```bash
./gradlew :app:assembleDebug
./gradlew :feature:<name>:impl:test :app:testDebugUnitTest
./gradlew :feature:<name>:impl:validateDebugScreenshotTest
```

Запусти три команды из раздела «Как проверяется» в `docs/architecture.md`: для новой фичи вывод должен быть пустым.

## 6. После

- `docs/architecture.md`: добавь фичу в «Статус миграции» как переведённую.
- `project.md`: добавь модуль в схему и таблицу модулей, маршрут в таблицу маршрутов.
- CI: job `ui-test` в `.github/workflows/ci.yml` перечисляет модули явно. Предложи пользователю добавить `:feature:<name>:impl:connectedDebugAndroidTest`, но без согласия файл не меняй.
