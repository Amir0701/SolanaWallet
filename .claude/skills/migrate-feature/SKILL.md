---
name: migrate-feature
description: Переводит существующую фичу SolanaCryproWallet (auth, send, receive) на слои domain/data/presentation без изменения поведения. Используй, когда просят перевести, мигрировать или отрефакторить фичу под Clean Architecture (migrate feature, refactor to clean architecture).
---

# Перевод фичи на слои

Цель — та же фича с тем же поведением, разложенная по `docs/architecture.md`. Код слоёв — `.claude/skills/new-feature/templates.md`.

Переводи одну фичу за раз и только по просьбе.

## 1. Зафиксируй поведение

1. Прочитай весь `impl` фичи и все её тесты: `test`, `screenshotTest`, `androidTest`.
2. Выпиши сценарии из существующих тестов ViewModel. Это условия готовности: у каждого сценария после перевода должен быть тест-двойник.
3. Запусти `./gradlew :feature:<name>:impl:test :feature:<name>:impl:validateDebugScreenshotTest` до правок. Если что-то уже падает, остановись и сообщи.

Составь план в plan mode: какая логика ViewModel уходит в какой use case, какие методы сессии — в какой репозиторий.

## 2. Перенос

1. Создай `domain`, `data`, `di` по шаблонам.
2. Перенеси бизнес-правила из ViewModel в use case: разбор суммы, проверку пустых значений, разбор слов мнемоники. Переключение потока (`Dispatchers.IO` / `Default`) перенеси из ViewModel в репозиторий.
3. ViewModel и экран перенеси в пакет `presentation`. ViewModel теперь получает use case вместо `WalletSessionManager`. Публичный API `UiState` и сообщений не меняй без необходимости.
4. `<Name>NavGraph.kt` оставь в корневом пакете `impl`, чтобы импорты в `WalletNavHost` не менялись.

## 3. Тесты

- Сценарии валидации и разбора переезжают в тесты use case. Сценарии состояния и событий остаются в тесте ViewModel, с моками use case вместо `WalletSessionManager`.
- Тест ViewModel перенеси в `test/.../impl/presentation/`. `MainDispatcherRule` оставь в корневом тестовом пакете и импортируй.
- Скриншот-тест оставь с тем же именем класса и в том же пакете: путь к эталону зависит от них. Обнови только импорт `<Name>ScreenContent`.
- В `androidTest` обнови только импорты.
- В ответе дай таблицу «старый тест → новый тест». Ни один сценарий не пропадает молча.

## 4. Проверка

```bash
./gradlew :app:assembleDebug
./gradlew :feature:<name>:impl:test :app:testDebugUnitTest
./gradlew :feature:<name>:impl:validateDebugScreenshotTest
```

- `validateDebugScreenshotTest` должен пройти без обновления эталонов. Если он падает, ты изменил UI. Найди и исправь причину, эталоны не перезаписывай.
- Три команды из раздела «Как проверяется» в `docs/architecture.md` должны дать пустой вывод для этой фичи.

## 5. После

- Отметь фичу как переведённую в «Статус миграции» `docs/architecture.md`.
- Если это send, сверь `templates.md` с получившимся кодом и поправь расхождения.
- Ошибки, найденные по ходу, не исправляй молча. Перечисли их в ответе и спроси, чинить ли отдельной правкой.
