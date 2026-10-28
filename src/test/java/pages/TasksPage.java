package pages;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Канбан-доска: колонки — статусы, карточки — задачи. */
public class TasksPage extends BasePage {
  private static final String COLUMN = "//main//div[@data-rfd-droppable-id]";
  private static final String CARD = "//div[@data-rfd-draggable-id]";
  private static final String CARD_TITLE = "//div[contains(@class, 'MuiTypography-h5')]";
  private static final String ANNOUNCEMENTS_SCRIPT =
      "return Array.from(document.querySelectorAll('[aria-live]'))"
          + ".map((node) => node.textContent).join(' ');";

  private static final By COLUMN_TITLES = By.xpath(COLUMN + "/preceding-sibling::h6");
  private static final By CARD_TITLES = By.xpath(COLUMN + CARD + CARD_TITLE);
  private static final By CREATE = By.xpath("//main//a[@aria-label='Create']");
  private static final By SAVE = By.xpath("//main//form//button[@aria-label='Save']");
  private static final By DELETE = By.xpath("//main//form//button[@aria-label='Delete']");
  private static final By TITLE_INPUT = By.xpath("//main//form//input[@name='title']");
  private static final By CONTENT_INPUT = By.xpath("//main//form//textarea[@name='content']");
  private static final By STATUS_VALUE =
      By.xpath("//main//form//input[@name='status_id']/preceding-sibling::div");

  public TasksPage(WebDriver driver, String baseUrl) {
    super(driver, baseUrl);
  }

  public TasksPage open() {
    openRoute("tasks");
    return waitLoaded();
  }

  /** Ждёт видимые карточки: скрытую доску тест должен заметить. */
  public TasksPage waitLoaded() {
    wait.until(ExpectedConditions.urlMatches("#/tasks/?(\\?.*)?$"));
    visible(CARD_TITLES);
    return this;
  }

  public List<String> columnTitles() {
    return visibleTexts(COLUMN_TITLES);
  }

  public List<String> cardTitles() {
    return visibleTexts(CARD_TITLES);
  }

  private static String column(String status) {
    return COLUMN + "[preceding-sibling::h6[normalize-space()=" + xpathLiteral(status) + "]]";
  }

  private static String card(String title, String scope) {
    return scope + CARD + "[." + CARD_TITLE + "[normalize-space()=" + xpathLiteral(title) + "]]";
  }

  private static String card(String title) {
    return card(title, "//main");
  }

  public List<String> cardsIn(String status) {
    visible(By.xpath(column(status)));
    return driver.findElements(By.xpath(column(status) + CARD + CARD_TITLE)).stream()
        .filter(WebElement::isDisplayed)
        .map(WebElement::getText)
        .toList();
  }

  public void waitCardIn(String title, String status) {
    visible(By.xpath(card(title, column(status))));
  }

  public void waitCardGone(String title) {
    waitHidden(By.xpath(card(title)));
  }

  public String statusOf(String title) {
    WebElement column = visible(By.xpath(card(title) + "/ancestor::div[@data-rfd-droppable-id]"));
    return column.findElement(By.xpath("./preceding-sibling::h6")).getText();
  }

  public String cardText(String title) {
    return visible(By.xpath(card(title))).getText();
  }

  /** Поля title и content формы создания. */
  public List<WebElement> openCreateForm() {
    click(CREATE);
    wait.until(ExpectedConditions.urlContains("#/tasks/create"));
    return List.of(visible(TITLE_INPUT), visible(CONTENT_INPUT));
  }

  /** Создаёт задачу и возвращается на доску. */
  public TasksPage create(String title, String content, String assignee, String status) {
    openCreateForm();
    chooseOption("assignee_id", assignee, "//main//form");
    fill(TITLE_INPUT, title);
    fill(CONTENT_INPUT, content);
    chooseOption("status_id", status, "//main//form");
    click(SAVE);
    // после создания react-admin открывает форму редактирования новой задачи
    wait.until(ExpectedConditions.urlMatches("#/tasks/\\d+$"));
    return open();
  }

  public Map<String, String> openEdit(String title) {
    click(By.xpath(card(title) + "//a[@aria-label='Edit']"));
    wait.until(ExpectedConditions.urlMatches("#/tasks/\\d+$"));
    return formValues();
  }

  /** Значения формы; задача и список статусов подгружаются асинхронно. */
  public Map<String, String> formValues() {
    WebElement title = visible(TITLE_INPUT);
    WebElement status = visible(STATUS_VALUE);
    wait.until(d -> !title.getDomProperty("value").isEmpty() && !visibleText(status).isEmpty());
    Map<String, String> values = new LinkedHashMap<>();
    values.put("title", title.getDomProperty("value"));
    values.put("content", visible(CONTENT_INPUT).getDomProperty("value"));
    values.put("status", visibleText(status));
    return values;
  }

  public TasksPage rename(String cardTitle, String newTitle, String newContent) {
    openEdit(cardTitle);
    fill(TITLE_INPUT, newTitle);
    fill(CONTENT_INPUT, newContent);
    click(SAVE);
    return waitLoaded();
  }

  public TasksPage changeStatus(String cardTitle, String status) {
    openEdit(cardTitle);
    chooseOption("status_id", status, "//main//form");
    click(SAVE);
    return waitLoaded();
  }

  public TasksPage delete(String title) {
    openEdit(title);
    click(DELETE);
    return waitLoaded();
  }

  /** Текст карточки на странице просмотра задачи. */
  public String openShow(String title) {
    click(By.xpath(card(title) + "//a[@aria-label='Show']"));
    wait.until(ExpectedConditions.urlMatches("#/tasks/\\d+/show$"));
    return visible(By.xpath("//main//form")).getText();
  }

  public TasksPage filterBy(String field, String option) {
    chooseOption(field, option, "//main//form");
    wait.until(ExpectedConditions.urlContains(field + "%22"));
    return waitLoaded();
  }

  /**
   * Перетаскивает карточку в соседнюю колонку справа с клавиатуры: доска построена
   * на @hello-pangea/dnd — пробел поднимает карточку, стрелка переносит, повторный пробел бросает.
   */
  public void moveCardRight(String title) {
    WebElement card = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(card(title))));
    card.sendKeys(Keys.SPACE);
    waitAnnouncement("lifted");
    card.sendKeys(Keys.ARROW_RIGHT);
    waitAnnouncement("moved");
    card.sendKeys(Keys.SPACE);
    waitAnnouncement("dropped");
  }

  private void waitAnnouncement(String word) {
    wait.until(
        d ->
            String.valueOf(((JavascriptExecutor) d).executeScript(ANNOUNCEMENTS_SCRIPT))
                .contains(word));
  }
}
