package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import pages.LabelsPage;
import pages.ResourcePage;

class LabelsTest extends LoggedInTest {
  private ResourcePage labels;

  @BeforeEach
  void openLabels() {
    labels = new LabelsPage(driver, baseUrl).open();
  }

  @Test
  void testListShowsAllLabels() {
    assertEquals(TestData.LABELS.size(), labels.rowCount());
    assertEquals(TestData.LABELS, labels.column("name"));
  }

  @Test
  void testCreateFormOpens() {
    WebElement name = labels.openCreateForm().getFirst();

    assertEquals("name", name.getDomAttribute("name"));
    assertEquals("", name.getDomProperty("value"));
  }

  @Test
  void testCreateLabel() {
    String name = "urgent-" + unique();

    labels.create(Map.of("name", name));
    labels.open();

    assertTrue(labels.hasRow(name));
    List<String> expected = new ArrayList<>(TestData.LABELS);
    expected.add(name);
    assertEquals(expected, labels.column("name"));
  }

  @Test
  void testEditLabel() {
    String name = "improvement-" + unique();
    assertEquals(Map.of("name", "enhancement"), labels.openEdit("enhancement"));
    labels.open();

    labels.update("enhancement", Map.of("name", name));

    assertTrue(labels.hasRow(name));
    labels.waitRowGone("enhancement");
    assertEquals(TestData.LABELS.size(), labels.rowCount());
  }

  @Test
  void testDeleteLabel() {
    labels.delete("bug");

    labels.waitRowGone("bug");
    assertEquals(TestData.LABELS.subList(1, TestData.LABELS.size()), labels.column("name"));
  }

  @Test
  void testDeleteSelectedLabels() {
    labels.select("task");
    labels.select("critical");

    labels.bulkDelete();

    labels.waitRowGone("critical");
    assertEquals(List.of("bug", "feature", "enhancement"), labels.column("name"));
  }

  @Test
  void testDeleteAllLabels() {
    labels.selectAll();
    labels.bulkDelete();

    assertEquals("No Labels yet.", labels.emptyMessage());
    assertTrue(labels.hasNoRows());
  }
}
