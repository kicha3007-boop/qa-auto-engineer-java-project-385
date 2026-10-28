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
import pages.StatusesPage;

class StatusesTest extends LoggedInTest {
  private ResourcePage statuses;

  @BeforeEach
  void openStatuses() {
    statuses = new StatusesPage(driver, baseUrl).open();
  }

  @Test
  void testListShowsAllStatuses() {
    assertEquals(TestData.STATUSES.size(), statuses.rowCount());
    assertEquals(List.copyOf(TestData.STATUSES.keySet()), statuses.column("name"));
    assertEquals(List.copyOf(TestData.STATUSES.values()), statuses.column("slug"));
  }

  @Test
  void testCreateFormOpens() {
    List<WebElement> fields = statuses.openCreateForm();

    assertEquals(
        List.of("name", "slug"),
        fields.stream().map(field -> field.getDomAttribute("name")).toList());
    assertTrue(fields.stream().allMatch(WebElement::isEnabled));
  }

  @Test
  void testCreateStatus() {
    String suffix = unique();
    String name = "In Progress " + suffix;

    statuses.create(Map.of("name", name, "slug", "in_progress_" + suffix));
    statuses.open();

    assertTrue(statuses.hasRow(name));
    assertTrue(statuses.column("slug").contains("in_progress_" + suffix));
    assertEquals(TestData.STATUSES.size() + 1, statuses.rowCount());
  }

  @Test
  void testEditStatus() {
    String suffix = unique();
    assertEquals(Map.of("name", "To Review", "slug", "to_review"), statuses.openEdit("To Review"));
    statuses.open();

    statuses.update("To Review", Map.of("name", "Review " + suffix, "slug", "review_" + suffix));

    assertTrue(statuses.hasRow("Review " + suffix));
    statuses.waitRowGone("To Review");
    assertTrue(statuses.column("slug").contains("review_" + suffix));
  }

  @Test
  void testDeleteStatus() {
    statuses.delete("To Publish");

    statuses.waitRowGone("To Publish");
    assertFalse(statuses.column("name").contains("To Publish"));
    assertEquals(TestData.STATUSES.size() - 1, statuses.rowCount());
  }

  @Test
  void testDeleteSelectedStatuses() {
    statuses.select("Draft");
    statuses.select("Published");

    statuses.bulkDelete();

    statuses.waitRowGone("Draft");
    statuses.waitRowGone("Published");
    assertEquals(List.of("To Review", "To Be Fixed", "To Publish"), statuses.column("name"));
  }

  @Test
  void testDeleteAllStatuses() {
    statuses.selectAll();
    statuses.bulkDelete();

    assertEquals("No Task statuses yet.", statuses.emptyMessage());
    assertTrue(statuses.hasNoRows());
  }
}
