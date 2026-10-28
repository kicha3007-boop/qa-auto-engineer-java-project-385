package pages;

import java.util.List;
import org.openqa.selenium.WebDriver;

public class LabelsPage extends ResourcePage {
  public LabelsPage(WebDriver driver, String baseUrl) {
    super(driver, baseUrl);
  }

  @Override
  protected String route() {
    return "labels";
  }

  @Override
  protected String emptyText() {
    return "No Labels yet.";
  }

  @Override
  protected List<String> formFields() {
    return List.of("name");
  }
}
