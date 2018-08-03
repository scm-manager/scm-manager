package sonia.scm.it;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.web.VndMediaType;

import java.util.List;

import static java.util.Arrays.asList;
import static sonia.scm.it.RestUtil.createResourceUrl;
import static sonia.scm.it.RestUtil.given;

public class TestData {

  private static final Logger LOG = LoggerFactory.getLogger(TestData.class);

  private static final List<String> PROTECTED_USERS = asList("scmadmin", "anonymous");

  public static void cleanup() {
    cleanupRepositories();
    cleanupGroups();
    cleanupUsers();
  }

  public static void cleanupRepositories() {
    List<String> repositories = given(VndMediaType.REPOSITORY_COLLECTION)
      .when()
      .get(createResourceUrl("repositories"))
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .body().jsonPath().getList("_embedded.repositories._links.self.href");
    LOG.info("about to delete {} repositories", repositories.size());
    repositories.forEach(TestData::delete);
  }

  public static void cleanupGroups() {
    List<String> groups = given(VndMediaType.GROUP_COLLECTION)
      .when()
      .get(createResourceUrl("groups"))
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .body().jsonPath().getList("_embedded.groups._links.self.href");
    LOG.info("about to delete {} groups", groups.size());
    groups.forEach(TestData::delete);
  }

  public static void cleanupUsers() {
    List<String> users = given(VndMediaType.USER_COLLECTION)
      .when()
      .get(createResourceUrl("users"))
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .body().jsonPath().getList("_embedded.users._links.self.href");
    LOG.info("about to delete {} users", users.size());
    users.stream().filter(url -> PROTECTED_USERS.stream().noneMatch(url::contains)).forEach(TestData::delete);
  }

  public static void delete(String url) {
    given(VndMediaType.REPOSITORY)
      .when()
      .delete(url)
      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);
    LOG.info("deleted {}", url);
  }

  public static void main(String[] args) {
    cleanup();
  }
}
