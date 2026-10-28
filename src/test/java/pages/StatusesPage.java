package pages;

import java.util.List;
import org.openqa.selenium.WebDriver;

public class StatusesPage extends ResourcePage {
  public StatusesPage(WebDriver driver, String baseUrl) {
    super(driver, baseUrl);
  }

  @Override
  protected String route() {
    return "task_statuses";
  }

  @Override
  protected String emptyText() {
    return "No Task statuses yet.";
  }

  @Override
  protected List<String> formFields() {
    return List.of("name", "slug");
  }
}
