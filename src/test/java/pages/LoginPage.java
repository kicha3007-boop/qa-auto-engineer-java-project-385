package pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage extends BasePage {
  private static final By USERNAME = By.name("username");
  private static final By PASSWORD = By.name("password");
  private static final By SIGN_IN =
      By.xpath("//form//button[@type='submit'][normalize-space()='Sign in']");

  public LoginPage(WebDriver driver, String baseUrl) {
    super(driver, baseUrl);
  }

  public LoginPage open() {
    openRoute("login");
    visible(USERNAME);
    return this;
  }

  public void login(String username, String password) {
    fill(USERNAME, username);
    fill(PASSWORD, password);
    click(SIGN_IN);
  }

  /** Видимые поле логина, поле пароля и кнопка входа. */
  public List<WebElement> formElements() {
    return List.of(visible(USERNAME), visible(PASSWORD), visible(SIGN_IN));
  }
}
