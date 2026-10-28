package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import pages.LoginPage;

class BasicTest extends BaseUiTest {
  @Test
  @Tag("smoke")
  void testApplicationRenders() {
    LoginPage page = new LoginPage(driver, baseUrl);
    page.openRoute("");

    List<WebElement> form = page.formElements();

    assertEquals("text", form.get(0).getDomAttribute("type"));
    assertEquals("password", form.get(1).getDomAttribute("type"));
    assertEquals("sign in", form.get(2).getText().toLowerCase());
    assertTrue(form.get(2).isEnabled());
  }
}
