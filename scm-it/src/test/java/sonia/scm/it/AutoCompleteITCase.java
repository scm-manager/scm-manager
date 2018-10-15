package sonia.scm.it;

import org.junit.Before;
import org.junit.Test;
import sonia.scm.it.utils.ScmRequests;
import sonia.scm.it.utils.TestData;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoCompleteITCase {


  public static final String CREATED_USER_PREFIX = "user_";
  public static final String CREATED_GROUP_PREFIX = "group_";

  @Before
  public void init() {
    TestData.cleanup();
  }

  @Test
  public void adminShouldAutoCompleteUsers() {
    createUsers();
    ScmRequests.start()
      .given()
      .requestIndexResource(TestData.USER_SCM_ADMIN,TestData.USER_SCM_ADMIN)
      .assertStatusCode(200)
      .usingIndexResponse()
      .requestAutoCompleteUsers("user*")
      .assertStatusCode(200)
      .usingAutoCompleteResponse()
      .assertAutoCompleteResults(assertAutoCompleteResult(CREATED_USER_PREFIX));
  }

  @Test
  public void userShouldAutoCompleteUsersWithoutAdminPermission() {
    String username = "nonAdmin";
    String password = "pass";
    TestData.createUser(username, password, false, "xml", "email@e.de");
    createUsers();
    ScmRequests.start()
      .given()
      .requestIndexResource(username,password)
      .assertStatusCode(200)
      .usingIndexResponse()
      .requestAutoCompleteUsers("user*")
      .assertStatusCode(200)
      .usingAutoCompleteResponse()
      .assertAutoCompleteResults(assertAutoCompleteResult(CREATED_USER_PREFIX));
  }

  @Test
  public void adminShouldAutoCompleteGroups() {
    createGroups();
    ScmRequests.start()
      .given()
      .requestIndexResource(TestData.USER_SCM_ADMIN,TestData.USER_SCM_ADMIN)
      .assertStatusCode(200)
      .usingIndexResponse()
      .applyAutoCompleteGroups("group*")
      .assertStatusCode(200)
      .usingAutoCompleteResponse()
      .assertAutoCompleteResults(assertAutoCompleteResult(CREATED_GROUP_PREFIX));
  }

  @Test
  public void userShouldAutoCompleteGroupsWithoutAdminPermission() {
    String username = "nonAdminUser";
    String password = "pass";
    TestData.createNotAdminUser(username, password);
    createGroups();
    ScmRequests.start()
      .given()
      .requestIndexResource(username,password)
      .assertStatusCode(200)
      .usingIndexResponse()
      .applyAutoCompleteGroups("group*")
      .assertStatusCode(200)
      .usingAutoCompleteResponse()
      .assertAutoCompleteResults(assertAutoCompleteResult(CREATED_GROUP_PREFIX));
  }

  @SuppressWarnings("unchecked")
  private Consumer<List<Map>> assertAutoCompleteResult(String id) {
    return autoCompleteDtos -> {
      IntStream.range(0, 5).forEach(i -> {
        assertThat(autoCompleteDtos).as("return maximum 5 entries").hasSize(5);
        assertThat(autoCompleteDtos.get(i)).containsEntry("id", id + (i + 1));
        assertThat(autoCompleteDtos.get(i)).containsEntry("displayName", id + (i + 1));
      });
    };
  }

  private void createUsers() {
    IntStream.range(0, 6).forEach(i -> TestData.createUser(CREATED_USER_PREFIX + (i + 1), "pass", false, "xml", CREATED_USER_PREFIX + (i + 1) + "@scm-manager.org"));
  }

  private void createGroups() {
    IntStream.range(0, 6).forEach(i -> TestData.createGroup(CREATED_GROUP_PREFIX + (i + 1), CREATED_GROUP_PREFIX + (i + 1)));
  }

}
