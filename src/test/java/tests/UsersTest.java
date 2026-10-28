package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import pages.ResourcePage;
import pages.UsersPage;

class UsersTest extends LoggedInTest {
  private ResourcePage users;

  @BeforeEach
  void openUsers() {
    users = new UsersPage(driver, baseUrl).open();
  }

  @Test
  void testListShowsAllUsers() {
    assertEquals(TestData.USER_EMAILS.size(), users.rowCount());
    assertEquals(TestData.USER_EMAILS, users.column("email"));
  }

  @Test
  void testListShowsKeyFields() {
    assertTrue(users.headers().containsAll(List.of("Email", "First name", "Last name")));
    assertEquals(List.of("John", "Jack"), users.column("firstName").subList(0, 2));
    assertEquals(List.of("Doe", "Jons"), users.column("lastName").subList(0, 2));
  }

  @Test
  void testCreateFormOpens() {
    List<WebElement> fields = users.openCreateForm();

    assertEquals(
        List.of("email", "firstName", "lastName"),
        fields.stream().map(field -> field.getDomAttribute("name")).toList());
    assertTrue(fields.stream().allMatch(field -> field.getDomProperty("value").isEmpty()));
  }

  @Test
  void testCreateUser() {
    String suffix = unique();
    String email = "new-" + suffix + "@example.com";

    users.create(Map.of("email", email, "firstName", "Ivan", "lastName", "Petrov" + suffix));
    users.open();

    assertTrue(users.hasRow(email));
    assertEquals(TestData.USER_EMAILS.size() + 1, users.rowCount());
    assertTrue(users.column("lastName").contains("Petrov" + suffix));
  }

  @Test
  void testEditFormIsPrefilled() {
    assertEquals(
        Map.of("email", "jack@yahoo.com", "firstName", "Jack", "lastName", "Jons"),
        users.openEdit("jack@yahoo.com"));
  }

  @Test
  void testEditUser() {
    String email = "jack-" + unique() + "@example.com";

    users.update("jack@yahoo.com", Map.of("email", email, "firstName", "Jackie"));

    assertTrue(users.hasRow(email));
    users.waitRowGone("jack@yahoo.com");
    assertTrue(users.column("firstName").contains("Jackie"));
  }

  @Test
  void testEditRejectsInvalidEmail() {
    users.openEdit("jack@yahoo.com");
    users.fillForm(Map.of("email", "not-an-email"));
    users.save();

    assertTrue(users.hasFieldError("Incorrect email format"));
    users.open();
    assertTrue(users.hasRow("jack@yahoo.com"));
    assertFalse(users.column("email").contains("not-an-email"));
  }

  @Test
  void testDeleteUser() {
    users.delete("jane@gmail.com");

    users.waitRowGone("jane@gmail.com");
    assertEquals(TestData.USER_EMAILS.size() - 1, users.rowCount());
  }

  @Test
  void testDeleteSelectedUsers() {
    users.select("jack@yahoo.com");
    users.select("alice@hotmail.com");
    assertEquals("2 items selected", users.selectedCount());

    users.bulkDelete();

    users.waitRowGone("jack@yahoo.com");
    users.waitRowGone("alice@hotmail.com");
    assertEquals(TestData.USER_EMAILS.size() - 2, users.rowCount());
  }

  @Test
  void testDeleteAllUsers() {
    users.selectAll();
    assertEquals(TestData.USER_EMAILS.size() + " items selected", users.selectedCount());

    users.bulkDelete();

    assertEquals("No Users yet.", users.emptyMessage());
    assertTrue(users.hasNoRows());
  }
}
