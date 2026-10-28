package pages;

import java.util.List;
import org.openqa.selenium.WebDriver;

public class UsersPage extends ResourcePage {
  public UsersPage(WebDriver driver, String baseUrl) {
    super(driver, baseUrl);
  }

  @Override
  protected String route() {
    return "users";
  }

  @Override
  protected String emptyText() {
    return "No Users yet.";
  }

  @Override
  protected List<String> formFields() {
    return List.of("email", "firstName", "lastName");
  }
}
