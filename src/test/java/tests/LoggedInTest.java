package tests;

import org.junit.jupiter.api.BeforeEach;
import pages.DashboardPage;
import pages.LoginPage;

/** Тесты разделов: перед каждым — вход и ожидание дашборда. */
public abstract class LoggedInTest extends BaseUiTest {
  protected DashboardPage dashboard;

  @BeforeEach
  void signIn() {
    new LoginPage(driver, baseUrl).open().login(TestData.USERNAME, TestData.PASSWORD);
    dashboard = new DashboardPage(driver, baseUrl).waitLoaded();
  }
}
