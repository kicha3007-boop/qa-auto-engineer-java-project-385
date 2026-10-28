package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {
  public static final String WELCOME_TITLE = "Welcome to the administration";

  private static final By CONTENT =
      By.xpath("//main//*[normalize-space()='Lorem ipsum sic dolor amet...']");

  public DashboardPage(WebDriver driver, String baseUrl) {
    super(driver, baseUrl);
  }

  public DashboardPage waitLoaded() {
    visible(CONTENT);
    return this;
  }

  public String content() {
    return visible(CONTENT).getText();
  }
}
