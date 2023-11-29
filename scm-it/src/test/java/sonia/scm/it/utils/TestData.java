/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.it.utils;

import io.restassured.response.ValidatableResponse;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.util.SystemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static sonia.scm.it.utils.RestUtil.createResourceUrl;
import static sonia.scm.it.utils.RestUtil.given;
import static sonia.scm.it.utils.RestUtil.givenAnonymous;
import static sonia.scm.it.utils.ScmTypes.availableScmTypes;

public class TestData {

  private static final Logger LOG = LoggerFactory.getLogger(TestData.class);

  public static final String USER_SCM_ADMIN = "scmadmin";
  public static final String USER_ANONYMOUS = "_anonymous";

  public static final Collection<String> READ = asList("read", "pull");
  public static final Collection<String> WRITE = asList("read", "write", "pull", "push");
  public static final Collection<String> OWNER = asList("*");

  private static final List<String> PROTECTED_USERS = asList(USER_SCM_ADMIN, USER_ANONYMOUS);

  private static Map<String, String> DEFAULT_REPOSITORIES = new HashMap<>();
  public static final JsonObjectBuilder JSON_BUILDER = NullAwareJsonObjectBuilder.wrap(Json.createObjectBuilder());

  public static void createDefault() {
    cleanup();
    createDefaultRepositories();
  }

  public static void cleanup() {
    LOG.info("start to clean up to integration tests");
    cleanupConfig();
    cleanupRepositories();
    cleanupGroups();
    cleanupUsers();
  }

  public static String getDefaultRepositoryUrl(String repositoryType) {
    return DEFAULT_REPOSITORIES.get(repositoryType);
  }

  public static void createNotAdminUser(String username, String password) {
     createUser(username, password, false, "xml", "user1@scm-manager.org");
  }

  public static void createUser(String username, String password, boolean isAdmin, String type, final String email) {
    LOG.info("create user with username: {}", username);
    String admin = isAdmin ? "true" : "false";
    given(VndMediaType.USER)
      .when()
      .body(new StringBuilder()
        .append(" {\n")
        .append("    \"active\": true,\n")
        .append("    \"admin\": ").append(admin).append(",\n")
        .append("    \"creationDate\": \"2018-08-21T12:26:46.084Z\",\n")
        .append("    \"displayName\": \"").append(username).append("\",\n")
        .append("    \"mail\": \"" + email + "\",\n")
        .append("    \"name\": \"").append(username).append("\",\n")
        .append("    \"password\": \"").append(password).append("\",\n")
        .append("    \"type\": \"").append(type).append("\"\n")
        .append("  }").toString())
      .post(getUsersUrl())
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    if (isAdmin) {
      assignAdminPermissions(username);
    }
  }

  public static void assignAdminPermissions(String username) {
    LOG.info("assign admin permissions to user {}", username);
    given(VndMediaType.PERMISSION_COLLECTION)
      .when()
      .body("{'permissions': ['*']}".replaceAll("'", "\""))
      .put(getPermissionUrl(username))
      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  private static URI getPermissionUrl(String username) {
    return RestUtil.createResourceUrl(String.format("users/%s/permissions", username));
  }

  public static void createGroup(String groupName, String desc) {
    LOG.info("create group with group name: {} and description {}", groupName, desc);
    given(VndMediaType.GROUP)
      .when()
      .body(getGroupJson(groupName,desc))
      .post(getGroupsUrl())
      .then()
      .statusCode(HttpStatus.SC_CREATED)
    ;
  }

  public static void createUserPermission(String username, Collection<String> verbs, String repositoryType) {
    String defaultPermissionUrl = TestData.getDefaultPermissionUrl(USER_SCM_ADMIN, USER_SCM_ADMIN, repositoryType);
    LOG.info("create permission with name {} and verbs {} using the endpoint: {}", username, verbs, defaultPermissionUrl);
    given(VndMediaType.REPOSITORY_PERMISSION)
      .when()
      .body("{\n" +
        "\t\"verbs\": " + verbs.stream().collect(Collectors.joining("\",\"", "[\"", "\"]")) + ",\n" +
        "\t\"name\": \"" + username + "\",\n" +
        "\t\"groupPermission\": false\n" +
        "\t\n" +
        "}")
      .post(defaultPermissionUrl)
      .then()
      .statusCode(HttpStatus.SC_CREATED)
    ;
  }

  public static List<Map> getUserPermissions(String username, String password, String repositoryType) {
    return callUserPermissions(username, password, repositoryType, HttpStatus.SC_OK)
      .extract()
      .body().jsonPath().<Map>getList("_embedded.permissions");
  }

  public static ValidatableResponse callUserPermissions(String username, String password, String repositoryType, int expectedStatusCode) {
    return given(VndMediaType.REPOSITORY_PERMISSION, username, password)
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

  public static ValidatableResponse callAnonymousRepository(String repositoryType, int expectedStatusCode) {
    return givenAnonymous(VndMediaType.REPOSITORY)

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

    clearAnonymousUserPermissions();
  }

  private static void clearAnonymousUserPermissions() {
    given(VndMediaType.PERMISSION_COLLECTION).accept("application/json")
      .when()
      .body("{\"permissions\":[]}")
      .put(createResourceUrl("users/_anonymous/permissions"))
      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);
    LOG.info("deleted permissions for user _anonymous");
  }

  private static void cleanupConfig() {
    try {
      StoredConfig config = SystemReader.getInstance().getUserConfig();
      config.setString("init", null, "defaultBranch", "main");
      config.save();
    } catch (ConfigInvalidException | IOException e) {
      LOG.error("could not set default branch for git to 'main'", e);
    }

    given(VndMediaType.CONFIG).accept("application/json")
      .when()
      .body("{\n" +
        "  \"proxyPassword\": null,\n" +
        "  \"proxyPort\": 8080," +
        "  \"proxyServer\": \"proxy.mydomain.com\",\n" +
        "  \"proxyUser\": null,\n" +
        "  \"enableProxy\": false,\n" +
        "  \"realmDescription\": \"SONIA :: SCM Manager\",\n" +
        "  \"disableGroupingGrid\": false,\n" +
        "  \"dateFormat\": \"YYYY-MM-DD HH:mm:ss\",\n" +
        "  \"anonymousAccessEnabled\": false,\n" +
        "  \"anonymousMode\": \"OFF\",\n" +
        "  \"baseUrl\": \"http://localhost:8081/scm\",\n" +
        "  \"forceBaseUrl\": false,\n" +
        "  \"loginAttemptLimit\": -1,\n" +
        "  \"proxyExcludes\": [],\n" +
        "  \"skipFailedAuthenticators\": false,\n" +
        "  \"pluginUrl\": \"https://plugin-center-api.scm-manager.org/api/v1/plugins/{version}?os={os}&arch={arch}&jre={jre}\", \n" +
        "  \"loginAttemptLimitTimeout\": 300, \n" +
        "  \"enabledXsrfProtection\": true, \n" +
        "  \"enabledUserConverter\": false, \n" +
        "  \"namespaceStrategy\": \"UsernameNamespaceStrategy\", \n" +
        "  \"loginInfoUrl\": \"https://login-info.scm-manager.org/api/v1/login-info\",\n" +
        "  \"releaseFeedUrl\": \"https://scm-manager.org/download/rss.xml\",\n" +
        "  \"mailDomainName\": \"scm-manager.local\", \n" +
        "  \"enabledApiKeys\": \"true\"\n" +
        "}")
      .put(createResourceUrl("config"))
      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);
    LOG.info("wrote default configuration");
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
    return JSON_BUILDER
      .add("contact", "zaphod.beeblebrox@hitchhiker.com")
      .add("description", "Heart of Gold")
      .add("name", getDefaultRepoName(repositoryType))
      .add("type", repositoryType)
      .build().toString();
  }

  public static String getDefaultRepoName(String repositoryType) {
    return "HeartOfGold-" + repositoryType;
  }

  public static String getGroupJson(String groupname , String desc) {
    return JSON_BUILDER
      .add("name", groupname)
      .add("description", desc)
      .build().toString();
  }

  public static URI getGroupsUrl() {
    return RestUtil.createResourceUrl("groups/");
  }

  public static URI getUsersUrl() {
    return RestUtil.createResourceUrl("users/");
  }

  public static String createPasswordChangeJson(String oldPassword, String newPassword) {
    return JSON_BUILDER
      .add("oldPassword", oldPassword)
      .add("newPassword", newPassword)
      .build().toString();
  }

  public static void main(String[] args) {
    cleanup();
  }

}
