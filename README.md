# Тестирование канбан-доски с Selenium (Java)

[![hexlet-check](https://github.com/kicha3007-boop/qa-auto-engineer-java-project-385/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/kicha3007-boop/qa-auto-engineer-java-project-385/actions)
[![CI](https://github.com/kicha3007-boop/qa-auto-engineer-java-project-385/actions/workflows/ci.yml/badge.svg)](https://github.com/kicha3007-boop/qa-auto-engineer-java-project-385/actions/workflows/ci.yml)

UI-тесты на Selenium и JUnit 5 для учебного менеджера задач Хекслета (react-admin): вход и выход,
пользователи, статусы, метки и канбан-доска задач. Тесты построены на объектах страниц (Page Object):
локаторы живут в `src/test/java/pages`, проверки — в `src/test/java/tests`.

## Запуск

Нужны Java 21+, Docker и Chrome (или Chromium с chromedriver).

```bash
git clone https://github.com/kicha3007-boop/qa-auto-engineer-java-project-385.git
cd qa-auto-engineer-java-project-385

make start          # приложение в Docker: http://localhost:5173 (в отдельном терминале)
make test           # все тесты в headless-Chrome
make test-headed    # то же с видимым браузером
make lint           # форматирование (Spotless, google-java-format)
```

| Переменная | По умолчанию | Назначение |
|---|---|---|
| `APP_BASE_URL` | `http://localhost:5173` | адрес приложения |
| `HEADLESS` | `true` | `false` — показывать браузер |
| `BROWSER_WINDOW_SIZE` | `1440,900` | размер окна |
| `CHROME_BIN`, `CHROMEDRIVER_BIN` | `/usr/bin/chromium`, `/usr/bin/chromedriver`, если есть | иначе Selenium Manager найдёт Chrome сам |

Другой порт: `make start APP_PORT=5174` и `APP_BASE_URL=http://localhost:5174 make test`.

## Что проверяется

| Класс | Сценарии |
|---|---|
| `BasicTest` | приложение отрисовалось: форма входа видна и активна |
| `AuthTest` | вход (дашборд и меню), вход с любыми данными, выход, закрытые разделы после выхода |
| `UsersTest` | список и поля, форма создания, создание, заполненная форма редактирования, правка, проверка email, удаление одного, выбранных и всех |
| `StatusesTest` | список, форма, создание, правка, удаление одного, выбранных и всех |
| `LabelsTest` | список, форма, создание, правка, удаление одной, выбранных и всех |
| `TasksTest` | колонки и карточки доски, создание в нужной колонке, просмотр, правка, смена статуса в форме и перетаскиванием, удаление, фильтры по статусу, исполнителю и метке |

Каждый тест открывает свежий браузер: приложение хранит данные в памяти вкладки, поэтому тесты не зависят
друг от друга. Проверяется видимый результат (`visibilityOfElementLocated`, текст, число строк), а не
просто наличие узла в DOM.

## Устройство

- `pages/BasePage` — драйвер, адрес, явные ожидания, ввод в поля React и выбор в выпадающих списках MUI;
- `pages/ResourcePage` — общий раздел react-admin со списком и формами; `UsersPage`, `StatusesPage`,
  `LabelsPage` задают только маршрут и поля;
- `pages/TasksPage` — канбан-доска, перетаскивание карточки с клавиатуры (@hello-pangea/dnd);
- `tests/BaseUiTest` — жизненный цикл браузера, `tests/LoggedInTest` — вход перед тестами разделов.
