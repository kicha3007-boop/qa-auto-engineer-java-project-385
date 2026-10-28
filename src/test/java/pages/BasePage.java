package pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Общие действия со страницами react-admin: ожидания, ввод, выпадающие списки. */
public abstract class BasePage {
  protected static final Duration TIMEOUT = Duration.ofSeconds(10);

  // пустой выпадающий список MUI содержит невидимый пробел нулевой ширины
  private static final String ZERO_WIDTH_SPACE = "​";

  private static final By TITLE = By.id("react-admin-title");
  private static final By PROFILE_BUTTON = By.cssSelector("header button[aria-label='Profile']");
  private static final By LOGOUT_ITEM =
      By.xpath("//li[@role='menuitem'][normalize-space()='Logout']");
  private static final By MENU_ITEMS = By.cssSelector("main ul[role='menu'] a[role='menuitem']");
  private static final String OPTION =
      "//ul[@role='listbox']//li[@role='option'][normalize-space()=%s]";

  protected final WebDriver driver;
  protected final String baseUrl;
  protected final WebDriverWait wait;

  // для шагов, которые сами повторяют действие, пока страница перерисовывается
  private final FluentWait<WebDriver> retryingWait;

  protected BasePage(WebDriver driver, String baseUrl) {
    this.driver = driver;
    this.baseUrl = baseUrl;
    this.wait = new WebDriverWait(driver, TIMEOUT);
    this.retryingWait =
        new FluentWait<>(driver)
            .withTimeout(TIMEOUT)
            .pollingEvery(Duration.ofMillis(300))
            .ignoring(NoSuchElementException.class)
            .ignoring(StaleElementReferenceException.class)
            .ignoring(ElementNotInteractableException.class)
            .ignoring(ElementClickInterceptedException.class);
  }

  /** Строка в кавычках для XPath: апостроф внутри текста не ломает выражение. */
  public static String xpathLiteral(String text) {
    return text.contains("'") ? "\"" + text + "\"" : "'" + text + "'";
  }

  /** Локатор элемента с точным видимым текстом. */
  public static By byText(String text) {
    return By.xpath("//*[normalize-space()=" + xpathLiteral(text) + "]");
  }

  public static String visibleText(WebElement element) {
    return element.getText().replace(ZERO_WIDTH_SPACE, "").strip();
  }

  public void openRoute(String route) {
    driver.get(baseUrl + "/#/" + route);
  }

  public String currentUrl() {
    return driver.getCurrentUrl();
  }

  public void waitUrlContains(String part) {
    wait.until(ExpectedConditions.urlContains(part));
  }

  protected WebElement visible(By locator) {
    return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
  }

  protected List<WebElement> allVisible(By locator) {
    visible(locator);
    return driver.findElements(locator).stream().filter(WebElement::isDisplayed).toList();
  }

  protected List<String> visibleTexts(By locator) {
    return allVisible(locator).stream().map(WebElement::getText).toList();
  }

  protected void click(By locator) {
    wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
  }

  protected void waitHidden(By locator) {
    wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
  }

  protected void fill(By locator, String value) {
    WebElement field = wait.until(ExpectedConditions.elementToBeClickable(locator));
    // clear() не вызывает onChange у React-полей, поэтому стираем как пользователь
    field.sendKeys(Keys.chord(Keys.CONTROL, "a"));
    field.sendKeys(Keys.DELETE);
    field.sendKeys(value);
  }

  /**
   * Выбирает значение в выпадающем списке MUI и ждёт, пока оно отобразится в поле. Список
   * react-admin подгружает асинхронно и перерисовывает, поэтому клик по варианту может не дойти:
   * тогда список открывается заново.
   */
  protected void chooseOption(String fieldName, String optionText, String scope) {
    By combobox =
        By.xpath(
            scope + "//input[@name='" + fieldName + "']/preceding-sibling::div[@role='combobox']");
    By option = By.xpath(OPTION.formatted(xpathLiteral(optionText)));

    retryingWait.until(
        d -> {
          if (visibleText(d.findElement(combobox)).equals(optionText)) {
            return true;
          }
          List<WebElement> options =
              d.findElements(option).stream().filter(WebElement::isDisplayed).toList();
          if (!options.isEmpty()) {
            options.getFirst().click();
          } else if (d.findElements(By.xpath("//ul[@role='listbox']")).isEmpty()) {
            d.findElement(combobox).click();
          }
          return false;
        });
    waitHidden(option);
  }

  public String title() {
    return visible(TITLE).getText();
  }

  public List<String> menuItems() {
    return visibleTexts(MENU_ITEMS);
  }

  public void logout() {
    click(PROFILE_BUTTON);
    click(LOGOUT_ITEM);
  }
}
