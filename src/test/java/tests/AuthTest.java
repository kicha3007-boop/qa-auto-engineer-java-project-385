package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import pages.DashboardPage;
import pages.LoginPage;

class AuthTest extends BaseUiTest {
  private LoginPage loginPage() {
    return new LoginPage(driver, baseUrl).open();
  }

  @Test
  void testSignIn() {
    loginPage().login(TestData.USERNAME, TestData.PASSWORD);

    DashboardPage dashboard = new DashboardPage(driver, baseUrl).waitLoaded();

    assertEquals("Lorem ipsum sic dolor amet...", dashboard.content());
    assertEquals(DashboardPage.WELCOME_TITLE, dashboard.title());
    assertEquals(TestData.MENU, dashboard.menuItems());
  }

  @Test
  void testSignInWithAnyCredentials() {
    loginPage().login("someone@example.com", "another-password");

    DashboardPage dashboard = new DashboardPage(driver, baseUrl).waitLoaded();

    assertEquals(TestData.MENU, dashboard.menuItems());
  }

  @Test
  void testLogout() {
    loginPage().login(TestData.USERNAME, TestData.PASSWORD);
    new DashboardPage(driver, baseUrl).waitLoaded().logout();

    LoginPage login = new LoginPage(driver, baseUrl);
    assertTrue(login.formElements().stream().allMatch(WebElement::isDisplayed));
    assertTrue(login.currentUrl().endsWith("#/login"));
  }

  @Test
  void testPagesRequireLoginAfterLogout() {
    loginPage().login(TestData.USERNAME, TestData.PASSWORD);
    new DashboardPage(driver, baseUrl).waitLoaded().logout();
    LoginPage login = new LoginPage(driver, baseUrl);
    login.formElements();

    login.openRoute("tasks");

    login.waitUrlContains("#/login");
    assertTrue(login.formElements().stream().allMatch(WebElement::isDisplayed));
  }
}
