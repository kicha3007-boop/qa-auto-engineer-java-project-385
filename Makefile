APP_IMAGE := hexletprojects/qa_auto_java_testing_kanban_board_project_ru_app
APP_PORT ?= 5173

# Приложение под тестами: http://localhost:$(APP_PORT)
start:
	docker run --rm -p $(APP_PORT):5173 $(APP_IMAGE)

# Тесты против запущенного приложения (APP_BASE_URL, по умолчанию http://localhost:5173).
test:
	./gradlew test

# С видимым браузером — удобно отлаживать локаторы.
test-headed:
	HEADLESS=false ./gradlew test

lint:
	./gradlew spotlessCheck

lint-fix:
	./gradlew spotlessApply

.PHONY: start test test-headed lint lint-fix
