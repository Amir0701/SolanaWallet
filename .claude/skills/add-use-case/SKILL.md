---
name: add-use-case
description: Добавляет новую операцию в существующую фичу SolanaCryproWallet через все слои — use case, репозиторий, ViewModel, экран, строки и тесты. Используй, когда в уже существующем экране нужно новое действие или новые данные (add use case, new action, new operation).
---

# Новая операция в фиче

Правила слоёв — `docs/architecture.md`. Код слоёв — `.claude/skills/new-feature/templates.md`.

## 0. Проверь статус фичи

Посмотри таблицу «Статус миграции» в `docs/architecture.md`.

- Фича переведена — продолжай.
- Фича не переведена — остановись и спроси пользователя: сначала перевести её (skill `migrate-feature`) или добавить операцию в текущем стиле фичи. Сам не переводи и не смешивай стили в одной фиче.

## 1. Уточни

- Что делает операция, какие исходы возможны (успех и каждый вид ошибки).
- Какой метод `WalletSessionManager` она использует. Если подходящего нет, остановись и согласуй изменение `:core:wallet`.

## 2. Код снизу вверх

1. `domain/model`: `<Action>Result` со всеми исходами.
2. `domain/repository`: метод в существующем интерфейсе, если он про то же, иначе новый интерфейс.
3. `domain/usecase`: `<Verb><Noun>UseCase`, бизнес-правила здесь.
4. `data`: реализация метода, `withContext(Dispatchers.IO)` для RPC или `Dispatchers.Default` для мнемоники.
5. `di`: `@Binds`, только если появился новый интерфейс.
6. `presentation`: use case в конструктор ViewModel, поля в `UiState`, новые события в sealed-тип сообщений, обработка каждого исхода.
7. `<Name>ScreenContent`: новые параметры и лямбды с значением. `<Name>Screen` передаёт их из ViewModel.
8. `strings.xml` фичи: новые строки на русском.

## 3. Тесты

- Новый `<Verb><Noun>UseCaseTest`: каждая ветка `<Action>Result`.
- Тест нового метода репозитория.
- `<Name>ViewModelTest`: состояние и событие для каждого исхода. Существующие тесты ViewModel обнови под новый конструктор, сценарии не удаляй.
- Изменился вид экрана — обнови вызов в скриншот-тесте. Эталоны не перезаписывай без согласия пользователя: покажи, что `validateDebugScreenshotTest` упал из-за ожидаемого изменения, и спроси.

## 4. Проверка

```bash
./gradlew :app:assembleDebug
./gradlew :feature:<name>:impl:test
./gradlew :feature:<name>:impl:validateDebugScreenshotTest
```

И три команды из раздела «Как проверяется» в `docs/architecture.md`.
