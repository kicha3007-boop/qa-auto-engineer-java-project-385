plugins {
    java
    id("com.diffplug.spotless") version "8.0.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        // Сборка и тесты на той Java, что установлена; байткод — под Java 21.
        languageVersion = JavaLanguageVersion.current()
    }
}

tasks.withType<JavaCompile> {
    options.release = 21
    options.encoding = "UTF-8"
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.35.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    // Адрес приложения и настройки браузера тесты берут из окружения.
    environment("APP_BASE_URL", System.getenv("APP_BASE_URL") ?: "http://localhost:5173")
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat()
        removeUnusedImports()
    }
}
