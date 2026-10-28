package tests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Жизненный цикл браузера: свежий Chrome перед каждым тестом и закрытие после. Тесты знают о
 * приложении только его адрес — переменную окружения APP_BASE_URL.
 */
public abstract class BaseUiTest {
  // Пути к chromium и chromedriver из пакетов Debian/Ubuntu. Если их нет,
  // Selenium Manager сам найдёт установленный Chrome и подходящий драйвер.
  private static final String[] CHROME_BINARIES = {
    "/usr/bin/chromium", "/usr/bin/chromium-browser"
  };
  private static final String[] CHROMEDRIVER_BINARIES = {"/usr/bin/chromedriver"};

  protected WebDriver driver;
  protected String baseUrl;

  @BeforeEach
  void startBrowser() {
    baseUrl = baseUrl();
    driver = new ChromeDriver(driverService(), chromeOptions());
    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
  }

  @AfterEach
  void quitBrowser() {
    if (driver != null) {
      driver.quit();
    }
  }

  /** Короткий суффикс, чтобы созданные записи не путались с исходными. */
  protected static String unique() {
    return UUID.randomUUID().toString().substring(0, 6);
  }

  private static String baseUrl() {
    String url = System.getenv("APP_BASE_URL");
    if (url == null || url.isBlank()) {
      throw new IllegalStateException("Задайте APP_BASE_URL, например http://localhost:5173");
    }
    return url.replaceAll("/+$", "");
  }

  private static ChromeOptions chromeOptions() {
    ChromeOptions options = new ChromeOptions();
    if (!"false".equalsIgnoreCase(System.getenv("HEADLESS"))) {
      options.addArguments("--headless=new");
    }
    options.addArguments(
        "--no-sandbox",
        "--disable-dev-shm-usage",
        "--disable-gpu",
        "--window-size=" + env("BROWSER_WINDOW_SIZE", "1440,900"));
    firstExisting("CHROME_BIN", CHROME_BINARIES).ifPresent(options::setBinary);
    return options;
  }

  private static ChromeDriverService driverService() {
    ChromeDriverService.Builder builder = new ChromeDriverService.Builder();
    firstExisting("CHROMEDRIVER_BIN", CHROMEDRIVER_BINARIES)
        .ifPresent(path -> builder.usingDriverExecutable(Path.of(path).toFile()));
    return builder.build();
  }

  private static Optional<String> firstExisting(String envName, String[] candidates) {
    return Optional.ofNullable(System.getenv(envName))
        .or(() -> Stream.of(candidates).filter(path -> Files.exists(Path.of(path))).findFirst());
  }

  private static String env(String name, String fallback) {
    String value = System.getenv(name);
    return value == null || value.isBlank() ? fallback : value;
  }
}
