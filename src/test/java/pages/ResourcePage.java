package pages;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Раздел react-admin со списком-таблицей и формами создания и редактирования. */
public abstract class ResourcePage extends BasePage {
  private static final By ROWS = By.xpath("//main//table/tbody/tr");
  private static final By HEADERS = By.xpath("//main//table/thead//th[@scope='col']");
  private static final By CREATE = By.xpath("//main//a[@aria-label='Create']");
  private static final By SAVE = By.xpath("//main//form//button[@aria-label='Save']");
  private static final By DELETE = By.xpath("//main//form//button[@aria-label='Delete']");
  // сам input у чекбокса MUI прозрачный, кликаем по его обёртке
  private static final By SELECT_ALL =
      By.xpath("//main//table/thead//input[@type='checkbox']/parent::span");
  private static final String BULK_TOOLBAR = "//main//*[@data-test='bulk-actions-toolbar']";
  private static final By BULK_DELETE = By.xpath(BULK_TOOLBAR + "//button[@aria-label='Delete']");
  private static final By SELECTED_COUNT = By.xpath(BULK_TOOLBAR + "//h6");

  protected ResourcePage(WebDriver driver, String baseUrl) {
    super(driver, baseUrl);
  }

  /** Маршрут раздела после «#/». */
  protected abstract String route();

  /** Подпись пустого списка. */
  protected abstract String emptyText();

  /** Имена полей формы по порядку. */
  protected abstract List<String> formFields();

  public ResourcePage open() {
    openRoute(route());
    return waitLoaded();
  }

  /** Ждёт видимые строки таблицы: скрытый раздел тест должен заметить. */
  public ResourcePage waitLoaded() {
    wait.until(ExpectedConditions.urlMatches("#/" + route() + "/?(\\?.*)?$"));
    visible(ROWS);
    return this;
  }

  public int rowCount() {
    return allVisible(ROWS).size();
  }

  /** Строк в таблице нет совсем — после удаления всех записей. */
  public boolean hasNoRows() {
    return driver.findElements(ROWS).isEmpty();
  }

  public List<String> column(String field) {
    return visibleTexts(
        By.xpath("//main//table/tbody/tr/td[contains(@class, 'column-" + field + "')]"));
  }

  public List<String> headers() {
    return visibleTexts(HEADERS).stream().filter(text -> !text.isEmpty()).toList();
  }

  private static By row(String text) {
    return By.xpath("//main//table/tbody/tr[td[normalize-space()=" + xpathLiteral(text) + "]]");
  }

  public boolean hasRow(String text) {
    return visible(row(text)).isDisplayed();
  }

  public void waitRowGone(String text) {
    waitHidden(row(text));
  }

  private static By field(String name) {
    return By.xpath("//main//form//*[@name='" + name + "']");
  }

  public List<WebElement> openCreateForm() {
    click(CREATE);
    wait.until(ExpectedConditions.urlContains("#/" + route() + "/create"));
    return formFields().stream().map(name -> visible(field(name))).toList();
  }

  public void fillForm(Map<String, String> values) {
    values.forEach((name, value) -> fill(field(name), value));
  }

  /** Значения полей формы; запись подгружается асинхронно, поэтому ждём, пока форма заполнится. */
  public Map<String, String> formValues() {
    WebElement first = visible(field(formFields().getFirst()));
    wait.until(d -> !first.getDomProperty("value").isEmpty());
    Map<String, String> values = new LinkedHashMap<>();
    formFields().forEach(name -> values.put(name, visible(field(name)).getDomProperty("value")));
    return values;
  }

  public void save() {
    click(SAVE);
  }

  /** Создаёт запись; после сохранения react-admin открывает форму редактирования новой записи. */
  public void create(Map<String, String> values) {
    openCreateForm();
    fillForm(values);
    save();
    wait.until(ExpectedConditions.urlMatches("#/" + route() + "/\\d+$"));
  }

  public Map<String, String> openEdit(String text) {
    click(By.xpath("//main//table/tbody/tr/td[normalize-space()=" + xpathLiteral(text) + "]"));
    wait.until(ExpectedConditions.urlMatches("#/" + route() + "/\\d+$"));
    return formValues();
  }

  public ResourcePage update(String text, Map<String, String> values) {
    openEdit(text);
    fillForm(values);
    save();
    return waitLoaded();
  }

  public ResourcePage delete(String text) {
    openEdit(text);
    click(DELETE);
    return waitLoaded();
  }

  public void select(String text) {
    click(
        By.xpath(
            "//main//table/tbody/tr[td[normalize-space()="
                + xpathLiteral(text)
                + "]]/td[1]//input[@type='checkbox']/parent::span"));
  }

  public void selectAll() {
    click(SELECT_ALL);
  }

  public String selectedCount() {
    return visible(SELECTED_COUNT).getText();
  }

  public void bulkDelete() {
    click(BULK_DELETE);
  }

  public String emptyMessage() {
    return visible(byText(emptyText())).getText();
  }

  public boolean hasFieldError(String text) {
    return visible(By.xpath("//main//form//p[normalize-space()=" + xpathLiteral(text) + "]"))
        .isDisplayed();
  }
}
