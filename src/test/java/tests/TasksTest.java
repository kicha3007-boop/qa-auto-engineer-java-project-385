package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import pages.TasksPage;

class TasksTest extends LoggedInTest {
  private TasksPage tasks;

  @BeforeEach
  void openBoard() {
    tasks = new TasksPage(driver, baseUrl).open();
  }

  private static List<String> sorted(List<String> items) {
    return items.stream().sorted().toList();
  }

  @Test
  void testBoardShowsColumnsForStatuses() {
    assertEquals(List.copyOf(TestData.STATUSES.keySet()), tasks.columnTitles());
  }

  @Test
  void testBoardShowsAllTasks() {
    List<String> all = TestData.TASKS_BY_STATUS.values().stream().flatMap(List::stream).toList();

    assertEquals(sorted(all), sorted(tasks.cardTitles()));
    TestData.TASKS_BY_STATUS.forEach(
        (status, titles) -> assertEquals(titles, tasks.cardsIn(status), status));
  }

  @Test
  void testCreateFormOpens() {
    List<WebElement> fields = tasks.openCreateForm();

    assertTrue(fields.stream().allMatch(field -> field.getDomProperty("value").isEmpty()));
  }

  @Test
  void testCreateTask() {
    String title = "Write tests " + unique();

    tasks.create(title, "Cover the board", "jane@gmail.com", "To Review");

    tasks.waitCardIn(title, "To Review");
    assertEquals("To Review", tasks.statusOf(title));
    assertTrue(tasks.cardText(title).contains("Cover the board"));
    List<String> expected = new ArrayList<>(TestData.TASKS_BY_STATUS.get("To Review"));
    expected.add(title);
    assertEquals(sorted(expected), sorted(tasks.cardsIn("To Review")));
  }

  @Test
  void testTaskDetails() {
    String details = tasks.openShow("Task 2");

    assertTrue(details.contains("Task 2"));
    assertTrue(details.contains("Description of task 2"));
    assertTrue(details.contains("john@google.com"));
  }

  @Test
  void testEditFormIsPrefilled() {
    assertEquals(
        Map.of("title", "Task 7", "content", "Description of task 7", "status", "To Review"),
        tasks.openEdit("Task 7"));
  }

  @Test
  void testEditTask() {
    String title = "Task 7 updated " + unique();

    tasks.rename("Task 7", title, "New description");

    tasks.waitCardGone("Task 7");
    assertEquals("To Review", tasks.statusOf(title));
    assertTrue(tasks.cardText(title).contains("New description"));
  }

  @Test
  void testChangeStatusInForm() {
    tasks.changeStatus("Task 1", "Published");

    tasks.waitCardIn("Task 1", "Published");
    assertEquals("Published", tasks.statusOf("Task 1"));
    assertFalse(tasks.cardsIn("To Be Fixed").contains("Task 1"));
  }

  @Test
  void testDragCardToNextColumn() {
    tasks.moveCardRight("Task 5");

    tasks.waitCardIn("Task 5", "To Review");
    assertFalse(tasks.cardsIn("Draft").contains("Task 5"));
    tasks.openRoute("");
    tasks.open();
    assertEquals("To Review", tasks.statusOf("Task 5"));
  }

  @Test
  void testDeleteTask() {
    tasks.delete("Task 8");

    tasks.waitCardGone("Task 8");
    assertEquals(List.of("Task 1", "Task 13"), tasks.cardsIn("To Be Fixed"));
  }

  @Test
  void testFilterByStatus() {
    tasks.filterBy("status_id", "Draft");

    tasks.waitCardGone("Task 1");
    assertEquals(sorted(TestData.TASKS_BY_STATUS.get("Draft")), sorted(tasks.cardTitles()));
  }

  @Test
  void testFilterByAssignee() {
    tasks.filterBy("assignee_id", "john@google.com");

    tasks.waitCardGone("Task 3");
    assertEquals(sorted(TestData.TASKS_OF_JOHN), sorted(tasks.cardTitles()));
  }

  @Test
  void testFilterByLabel() {
    tasks.filterBy("label_id", "critical");

    tasks.waitCardGone("Task 1");
    assertTrue(tasks.cardTitles().contains("Task 15"));
  }
}
