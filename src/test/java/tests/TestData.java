package tests;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Данные, с которыми приложение стартует, и учётные данные для входа. Приложение хранит записи в
 * памяти браузера, поэтому каждый тест начинает с одного и того же набора.
 */
final class TestData {
  static final String USERNAME = "admin";
  static final String PASSWORD = "secret";

  static final List<String> MENU =
      List.of("Dashboard", "Tasks", "Users", "Labels", "Task statuses");

  static final List<String> USER_EMAILS =
      List.of(
          "john@google.com",
          "jack@yahoo.com",
          "jane@gmail.com",
          "alice@hotmail.com",
          "peter@outlook.com",
          "sarah@example.com",
          "michael@example.com",
          "emily@example.com");

  static final Map<String, String> STATUSES =
      linkedMap(
          "Draft", "draft",
          "To Review", "to_review",
          "To Be Fixed", "to_be_fixed",
          "To Publish", "to_publish",
          "Published", "published");

  static final List<String> LABELS = List.of("bug", "feature", "enhancement", "task", "critical");

  static final Map<String, List<String>> TASKS_BY_STATUS =
      Map.of(
          "Draft", List.of("Task 11", "Task 5", "Task 6"),
          "To Review", List.of("Task 2", "Task 12", "Task 7"),
          "To Be Fixed", List.of("Task 1", "Task 13", "Task 8"),
          "To Publish", List.of("Task 3", "Task 14", "Task 9"),
          "Published", List.of("Task 4", "Task 15", "Task 10"));

  static final List<String> TASKS_OF_JOHN =
      List.of("Task 1", "Task 2", "Task 5", "Task 11", "Task 15");

  private TestData() {}

  private static Map<String, String> linkedMap(String... pairs) {
    Map<String, String> map = new LinkedHashMap<>();
    for (int i = 0; i < pairs.length; i += 2) {
      map.put(pairs[i], pairs[i + 1]);
    }
    return map;
  }
}
