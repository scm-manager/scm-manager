package sonia.scm.it;

import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.PermissionType;
import sonia.scm.web.VndMediaType;

import javax.json.Json;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static sonia.scm.it.RestUtil.createResourceUrl;
import static sonia.scm.it.RestUtil.given;
import static sonia.scm.it.ScmTypes.availableScmTypes;

public class TestData {

  private static final Logger LOG = LoggerFactory.getLogger(TestData.class);

  public static final String USER_SCM_ADMIN = "scmadmin";
  public static final String USER_ANONYMOUS = "anonymous";
  private static final List<String> PROTECTED_USERS = asList(USER_SCM_ADMIN, USER_ANONYMOUS);

  private static Map<String, String> DEFAULT_REPOSITORIES = new HashMap<>();

  public static void createDefault() {
    cleanup();
    createDefaultRepositories();
  }

  public static void cleanup() {
    LOG.info("start to clean up to integration tests");
    cleanupRepositories();
    cleanupGroups();
    cleanupUsers();
  }

  public static String getDefaultRepositoryUrl(String repositoryType) {
    return DEFAULT_REPOSITORIES.get(repositoryType);
  }

  public static void createUser(String username, String password) {
    LOG.info("create user with username: {}", username);
    given(VndMediaType.USER)
      .when()
      .content(" {\n" +
        "                \"active\": true,\n" +
        "                \"admin\": false,\n" +
        "                \"creationDate\": \"2018-08-21T12:26:46.084Z\",\n" +
        "                \"displayName\": \"" + username + "\",\n" +
        "                \"mail\": \"user1@scm-manager.org\",\n" +
        "                \"name\": \"" + username + "\",\n" +
        "                \"password\": \"" + password + "\",\n" +
        "                \"type\": \"xml\"\n" +
        "               \n" +
        "            }")
      .post(createResourceUrl("users"))
      .then()
      .statusCode(HttpStatus.SC_CREATED)
    ;
  }


  public static void createUserPermission(String name, PermissionType permissionType, String repositoryType) {
    String defaultPermissionUrl = TestData.getDefaultPermissionUrl(USER_SCM_ADMIN, USER_SCM_ADMIN, repositoryType);
    LOG.info("create permission with name {} and type: {} using the endpoint: {}", name, permissionType, defaultPermissionUrl);
    given(VndMediaType.PERMISSION)
      .when()
      .content("{\n" +
        "\t\"type\": \"" + permissionType.name() + "\",\n" +
        "\t\"name\": \"" + name + "\",\n" +
        "\t\"groupPermission\": false\n" +
        "\t\n" +
        "}")
      .post(defaultPermissionUrl)
      .then()
      .statusCode(HttpStatus.SC_CREATED)
    ;
  }

  public static List<Object> getUserPermissions(String username, String password, String repositoryType) {
    return callUserPermissions(username, password, repositoryType, HttpStatus.SC_OK)
      .extract()
      .body().jsonPath().getList("_embedded.permissions");
  }

  public static ValidatableResponse callUserPermissions(String username, String password, String repositoryType, int expectedStatusCode) {
    return given(VndMediaType.PERMISSION, username, password)
      .when()
      .get(TestData.getDefaultPermissionUrl(username, password, repositoryType))
      .then()
      .statusCode(expectedStatusCode);
  }

  public static ValidatableResponse callRepository(String username, String password, String repositoryType, int expectedStatusCode) {
    return given(VndMediaType.REPOSITORY, username, password)

      .when()
      .get(getDefaultRepositoryUrl(repositoryType))

      .then()
      .statusCode(expectedStatusCode);
  }

  public static String getDefaultPermissionUrl(String username, String password, String repositoryType) {
    return given(VndMediaType.REPOSITORY, username, password)
      .when()
      .get(getDefaultRepositoryUrl(repositoryType))
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .body().jsonPath().getString("_links.permissions.href");
  }


  private static void cleanupRepositories() {
    LOG.info("clean up repository");
    List<String> repositories = given(VndMediaType.REPOSITORY_COLLECTION)
      .when()
      .get(createResourceUrl("repositories"))
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .body().jsonPath().getList("_embedded.repositories._links.self.href");
    LOG.info("about to delete {} repositories", repositories.size());
    repositories.forEach(TestData::delete);
    DEFAULT_REPOSITORIES.clear();
  }

  private static void cleanupGroups() {
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

  private static void cleanupUsers() {
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

  private static void delete(String url) {
    given(VndMediaType.REPOSITORY)
      .when()
      .delete(url)
      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);
    LOG.info("deleted {}", url);
  }

  private static void createDefaultRepositories() {
    LOG.info("create default repositories");
    for (String repositoryType : availableScmTypes()) {
      String url = given(VndMediaType.REPOSITORY)
        .body(repositoryJson(repositoryType))

        .when()
        .post(createResourceUrl("repositories"))

        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .extract()
        .header("location");
      LOG.info("a {} repository is created: {}", repositoryType, url);
      DEFAULT_REPOSITORIES.put(repositoryType, url);
    }
  }

  public static String repositoryJson(String repositoryType) {
    return Json.createObjectBuilder()
      .add("contact", "zaphod.beeblebrox@hitchhiker.com")
      .add("description", "Heart of Gold")
      .add("name", "HeartOfGold-" + repositoryType)
      .add("archived", false)
      .add("type", repositoryType)
      .build().toString();
  }

  public static void main(String[] args) {
    cleanup();
  }
}
