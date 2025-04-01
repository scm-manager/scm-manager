/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.it.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.hamcrest.Matchers.is;
import static sonia.scm.it.utils.TestData.createPasswordChangeJson;


/**
 * Encapsulate rest requests of a repository in builder pattern
 * <p>
 * A Get Request can be applied with the methods request*()
 * These methods return a AppliedGet*Request object
 * This object can be used to apply general assertions over the rest Assured response
 * In the AppliedGet*Request classes there is a using*Response() method
 * that return the *Response class containing specific operations related to the specific response
 * the *Response class contains also the request*() method to apply the next GET request from a link in the response.
 */
@SuppressWarnings("rawtypes")
public class ScmRequests {

  private static final Logger LOG = LoggerFactory.getLogger(ScmRequests.class);

  private String username;
  private String password;

  public static ScmRequests start() {
    return new ScmRequests();
  }

  public IndexResponse requestIndexResource() {
    return new IndexResponse(applyGETRequest(RestUtil.REST_BASE_URL.toString()));
  }

  public IndexResponse requestIndexResource(String username, String password) {
    setUsername(username);
    setPassword(password);
    return new IndexResponse(applyGETRequest(RestUtil.REST_BASE_URL.toString()));
  }

  public UserResponse<UserResponse> requestUser(String username, String password, String pathParam) {
    setUsername(username);
    setPassword(password);
    return new UserResponse<>(applyGETRequest(RestUtil.REST_BASE_URL.resolve("users/" + pathParam).toString()), null);
  }

  public ChangePasswordResponse<ChangePasswordResponse> requestUserChangePassword(String username, String password, String userPathParam, String newPassword) {
    setUsername(username);
    setPassword(password);
    return new ChangePasswordResponse<>(applyPUTRequest(RestUtil.REST_BASE_URL.resolve("users/" + userPathParam + "/password").toString(), VndMediaType.PASSWORD_OVERWRITE, TestData.createPasswordChangeJson(password, newPassword)), null);
  }

  @SuppressWarnings("unchecked")
  public ModelResponse requestPluginTranslations(String language) {
    Response response = applyGETRequest(RestUtil.BASE_URL.resolve("locales/" + language + "/plugins.json").toString());
    return new ModelResponse(response, null);
  }

  public RequestSpecification withBasicAuth() {
    return RestAssured.given()
      .auth().preemptive().basic(username, password);
  }

  /**
   * Apply a GET Request to the extracted url from the given link
   *
   * @param linkPropertyName the property name of link
   * @param response         the response containing the link
   * @return the response of the GET request using the given link
   */
  private Response applyGETRequestFromLink(Response response, String linkPropertyName) {
    return applyGETRequestFromLinkWithParams(response, linkPropertyName, "");
  }

  /**
   * Apply a GET Request to the extracted url from the given link
   *
   * @param linkPropertyName the property name of link
   * @param response         the response containing the link
   * @param params           query params eg. ?q=xyz&count=12 or path params eg. namespace/name
   * @return the response of the GET request using the given link
   */
  private Response applyGETRequestFromLinkWithParams(Response response, String linkPropertyName, String params) {
    String url = response
      .then()
      .extract()
      .path(linkPropertyName);
    Assert.assertNotNull("no url found for link " + linkPropertyName, url);
    return applyGETRequestWithQueryParams(url, params);
  }

  /**
   * Apply a GET Request to the given <code>url</code> and return the response.
   *
   * @param url    the url of the GET request
   * @param params query params eg. ?q=xyz&count=12 or path params eg. namespace/name
   * @return the response of the GET request using the given <code>url</code>
   */
  private Response applyGETRequestWithQueryParams(String url, String params) {
    LOG.info("GET {}", url);
    if (username == null || password == null) {
      return RestAssured.given()
        .when()
        .get(url + params);
    }
    return withBasicAuth()
      .when()
      .get(url + params);
  }

  /**
   * Apply a GET Request to the given <code>url</code> and return the response.
   *
   * @param url the url of the GET request
   * @return the response of the GET request using the given <code>url</code>
   **/
  private Response applyGETRequest(String url) {
    return applyGETRequestWithQueryParams(url, "");
  }

  /**
   * Apply a PUT Request to the extracted url from the given link
   *
   * @param response         the response containing the link
   * @param linkPropertyName the property name of link
   * @return the response of the PUT request using the given link
   */
  private Response applyPUTRequestFromLink(Response response, String linkPropertyName, String content, String body) {
    return applyPUTRequest(response
      .then()
      .extract()
      .path(linkPropertyName), content, body);
  }


  /**
   * Apply a PUT Request to the given <code>url</code> and return the response.
   *
   * @param url the url of the PUT request
   * @return the response of the PUT request using the given <code>url</code>
   */
  private Response applyPUTRequest(String url, String mediaType, String body) {
    LOG.info("PUT {}", url);
    return withBasicAuth()
      .when()
      .contentType(mediaType)
      .accept(mediaType)
      .body(body)
      .put(url);
  }

  /**
   * Apply a PUT Request to the extracted url from the given link
   *
   * @param response         the response containing the link
   * @param linkPropertyName the property name of link
   * @return the response of the PUT request using the given link
   */
  private Response applyPOSTRequestFromLink(Response response, String linkPropertyName, String content, String body) {
    return applyPOSTRequest(response
      .then()
      .extract()
      .path(linkPropertyName), content, body);
  }


  /**
   * Apply a POST Request to the given <code>url</code> and return the response.
   *
   * @param url the url of the PUT request
   * @return the response of the PUT request using the given <code>url</code>
   */
  private Response applyPOSTRequest(String url, String mediaType, String body) {
    LOG.info("POST {}", url);
    return withBasicAuth()
      .when()
      .contentType(mediaType)
      .accept(mediaType)
      .body(body)
      .post(url);
  }

  private void setUsername(String username) {
    this.username = username;
  }

  private void setPassword(String password) {
    this.password = password;
  }

  public class IndexResponse extends ModelResponse<IndexResponse, IndexResponse> {
    public static final String LINK_AUTOCOMPLETE_USERS = "_links.autocomplete.find{it.name=='users'}.href";
    public static final String LINK_AUTOCOMPLETE_GROUPS = "_links.autocomplete.find{it.name=='groups'}.href";
    public static final String LINK_REPOSITORIES = "_links.repositories.href";
    public static final String LINK_REPOSITORY_TYPES = "_links.repositoryTypes.href";
    private static final String LINK_ME = "_links.me.href";
    private static final String LINK_USERS = "_links.users.href";

    public IndexResponse(Response response) {
      super(response, null);
    }

    public AutoCompleteResponse<IndexResponse> requestAutoCompleteUsers(String q) {
      return new AutoCompleteResponse<>(applyGETRequestFromLinkWithParams(response, LINK_AUTOCOMPLETE_USERS, "?q=" + q), this);
    }

    public AutoCompleteResponse<IndexResponse> requestAutoCompleteGroups(String q) {
      return new AutoCompleteResponse<>(applyGETRequestFromLinkWithParams(response, LINK_AUTOCOMPLETE_GROUPS, "?q=" + q), this);
    }

    public RepositoryTypeResponse<IndexResponse> requestRepositoryType(String type) {
      return new RepositoryTypeResponse<>(applyGETRequestFromLinkWithParams(response, LINK_REPOSITORY_TYPES, type), this);
    }

    public RepositoryResponse<IndexResponse> requestRepository(String namespace, String name) {
      return new RepositoryResponse<>(applyGETRequestFromLinkWithParams(response, LINK_REPOSITORIES, namespace + "/" + name), this);
    }

    public MeResponse<IndexResponse> requestMe() {
      return new MeResponse<>(applyGETRequestFromLink(response, LINK_ME), this);
    }

    public UserResponse<IndexResponse> requestUser(String username) {
      return new UserResponse<>(applyGETRequestFromLinkWithParams(response, LINK_USERS, username), this);
    }

    public IndexResponse assertUsersLinkDoesNotExists() {
      return super.assertPropertyPathDoesNotExists(LINK_USERS);
    }

    public String getUrl(String linkName) {
      return response
        .then()
        .extract()
        .path("_links." + linkName + ".href");
    }
  }

  public class RepositoryResponse<PREV extends ModelResponse> extends ModelResponse<RepositoryResponse<PREV>, PREV> {

    public static final String LINKS_SOURCES = "_links.sources.href";
    public static final String LINKS_CHANGESETS = "_links.changesets.href";

    public RepositoryResponse(Response response, PREV previousResponse) {
      super(response, previousResponse);
    }

    public SourcesResponse<RepositoryResponse> requestSources() {
      return new SourcesResponse<>(applyGETRequestFromLink(response, LINKS_SOURCES), this);
    }

    public ChangesetsResponse<RepositoryResponse> requestChangesets() {
      return new ChangesetsResponse<>(applyGETRequestFromLink(response, LINKS_CHANGESETS), this);
    }

    public FullExportResponse<RepositoryResponse> requestFullExport() {
      return new FullExportResponse<>(applyGETRequestFromLinkWithParams(response, "_links.fullExport.href", "?compressed=true"), this);
    }

    public void writeTestData(String value) {
      applyPOSTRequestFromLink(response, "_links.test-data.href", APPLICATION_JSON, "{\"value\":\"" + value + "\"}");
    }

    public TestDataResponse<RepositoryResponse> requestTestData() {
      return new TestDataResponse<>(applyGETRequestFromLink(response, "_links.test-data.href"), this);
    }
  }

  public class RepositoryTypeResponse<PREV extends ModelResponse> extends ModelResponse<RepositoryResponse<PREV>, PREV> {

    public RepositoryTypeResponse(Response response, PREV previousResponse) {
      super(response, previousResponse);
    }

    public void requestImport(String type, Path file, String repositoryName) {
      String url = response
        .then()
        .extract()
        .path("_links.import.find{it.name=='" + type + "'}.href");
      Assert.assertNotNull("no url found for link " + "_links.import.find{it.name=='" + type + "'}.href", url);

      LOG.info("POST for import to  {}", url);
      RepositoryDto repository = new RepositoryDto();
      repository.setType("git");
      repository.setName(repositoryName);
      withBasicAuth()
        .multiPart("bundle", file.toFile())
        .multiPart("repository", repository)
        .when()
        .post(url + "?compressed=true")
        .then()
        .statusCode(201);
      System.out.println("done");
    }
  }

  public class ChangesetsResponse<PREV extends ModelResponse> extends ModelResponse<ChangesetsResponse<PREV>, PREV> {

    public ChangesetsResponse(Response response, PREV previousResponse) {
      super(response, previousResponse);
    }

    public ChangesetsResponse assertChangesets(Consumer<List<Map>> changesetsConsumer) {
      List<Map> changesets = response.then().extract().path("_embedded.changesets");
      changesetsConsumer.accept(changesets);
      return this;
    }

    public DiffResponse<ChangesetsResponse> requestDiffInGitFormat(String revision) {
      return new DiffResponse<>(applyGETRequestFromLinkWithParams(response, "_embedded.changesets.find{it.id=='" + revision + "'}._links.diff.href", "?format=GIT"), this);
    }

    public ModificationsResponse<ChangesetsResponse> requestModifications(String revision) {
      return new ModificationsResponse<>(applyGETRequestFromLink(response, "_embedded.changesets.find{it.id=='" + revision + "'}._links.modifications.href"), this);
    }
  }


  public class SourcesResponse<PREV extends ModelResponse> extends ModelResponse<SourcesResponse<PREV>, PREV> {

    public SourcesResponse(Response response, PREV previousResponse) {
      super(response, previousResponse);
    }

    public SourcesResponse assertRevision(Consumer<String> assertRevision) {
      String revision = response.then().extract().path("revision");
      assertRevision.accept(revision);
      return this;
    }

    public SourcesResponse assertFiles(Consumer<List> assertFiles) {
      List files = response.then().extract().path("files");
      assertFiles.accept(files);
      return this;
    }

    public ChangesetsResponse<SourcesResponse> requestFileHistory(String fileName) {
      return new ChangesetsResponse<>(applyGETRequestFromLink(response, "_embedded.children.find{it.name=='" + fileName + "'}._links.history.href"), this);
    }

    public SourcesResponse<SourcesResponse> requestSelf(String fileName) {
      return new SourcesResponse<>(applyGETRequestFromLink(response, "_embedded.children.find{it.name=='" + fileName + "'}._links.self.href"), this);
    }
  }

  public class FullExportResponse<PREV extends ModelResponse> extends ModelResponse<FullExportResponse<PREV>, PREV> {

    public FullExportResponse(Response response, PREV previousResponse) {
      super(response, previousResponse);
    }

    public Path exportFile() {
      InputStream exportStream = response.asInputStream();
      try {
        Path tempFile = Files.createTempFile("scm-export", ".tgz");
        Files.copy(exportStream, tempFile, REPLACE_EXISTING);
        return tempFile;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public class TestDataResponse<PREV extends ModelResponse> extends ModelResponse<FullExportResponse<PREV>, PREV> {

    public TestDataResponse(Response response, PREV previousResponse) {
      super(response, previousResponse);
    }

    public List<String> getTestData() {
      return response.then().contentType(APPLICATION_JSON).extract().path("value");
    }
  }

  public class ModificationsResponse<PREV extends ModelResponse> extends ModelResponse<ModificationsResponse<PREV>, PREV> {

    public ModificationsResponse(Response response, PREV previousResponse) {
      super(response, previousResponse);
    }

    public ModificationsResponse<PREV> assertRevision(Consumer<String> assertRevision) {
      String revision = response.then().extract().path("revision");
      assertRevision.accept(revision);
      return this;
    }

    public ModificationsResponse<PREV> assertAdded(Consumer<List<String>> assertAdded) {
      List<String> added = response.then().extract().path("added");
      assertAdded.accept(added);
      return this;
    }

    public ModificationsResponse<PREV> assertRemoved(Consumer<List<String>> assertRemoved) {
      List<String> removed = response.then().extract().path("removed");
      assertRemoved.accept(removed);
      return this;
    }

    public ModificationsResponse<PREV> assertModified(Consumer<List<String>> assertModified) {
      List<String> modified = response.then().extract().path("modified");
      assertModified.accept(modified);
      return this;
    }

  }

  public class MeResponse<PREV extends ModelResponse> extends ModelResponse<MeResponse<PREV>, PREV> {

    public static final String LINKS_PASSWORD_HREF = "_links.password.href";

    public MeResponse(Response response, PREV previousResponse) {
      super(response, previousResponse);
    }

    public MeResponse<PREV> assertPasswordLinkDoesNotExists() {
      return assertPropertyPathDoesNotExists(LINKS_PASSWORD_HREF);
    }

    public ChangePasswordResponse<MeResponse> requestChangePassword(String oldPassword, String newPassword) {
      return new ChangePasswordResponse<>(applyPUTRequestFromLink(super.response, LINKS_PASSWORD_HREF, VndMediaType.PASSWORD_CHANGE, createPasswordChangeJson(oldPassword, newPassword)), this);
    }
  }

  public class UserResponse<PREV extends ModelResponse> extends ModelResponse<UserResponse<PREV>, PREV> {

    public static final String LINKS_PASSWORD_HREF = "_links.password.href";

    public UserResponse(Response response, PREV previousResponse) {
      super(response, previousResponse);
    }

    public UserResponse<PREV> assertPassword(Consumer<String> assertPassword) {
      return super.assertSingleProperty(assertPassword, "password");
    }

    public UserResponse<PREV> assertType(Consumer<String> assertType) {
      return assertSingleProperty(assertType, "type");
    }

    public UserResponse<PREV> assertPasswordLinkDoesNotExists() {
      return assertPropertyPathDoesNotExists(LINKS_PASSWORD_HREF);
    }

    public ChangePasswordResponse<UserResponse> requestChangePassword(String newPassword) {
      return new ChangePasswordResponse<>(applyPUTRequestFromLink(super.response, LINKS_PASSWORD_HREF, VndMediaType.PASSWORD_OVERWRITE, createPasswordChangeJson(null, newPassword)), this);
    }
  }


  /**
   * encapsulate standard assertions over model properties
   */
  public class ModelResponse<SELF extends ModelResponse<SELF, PREV>, PREV extends ModelResponse> {

    protected PREV previousResponse;
    protected Response response;

    public ModelResponse(Response response, PREV previousResponse) {
      this.response = response;
      this.previousResponse = previousResponse;
    }

    public Response getResponse() {
      return response;
    }

    public PREV returnToPrevious() {
      return previousResponse;
    }

    public <T> SELF assertSingleProperty(Consumer<T> assertSingleProperty, String propertyJsonPath) {
      T propertyValue = response.then().extract().path(propertyJsonPath);
      assertSingleProperty.accept(propertyValue);
      return (SELF) this;
    }

    public SELF assertPropertyPathExists(String propertyJsonPath) {
      response.then().assertThat().body("any { it.containsKey('" + propertyJsonPath + "')}", is(true));
      return (SELF) this;
    }

    public SELF assertPropertyPathDoesNotExists(String propertyJsonPath) {
      response.then().assertThat().body("this.any { it.containsKey('" + propertyJsonPath + "')}", is(false));
      return (SELF) this;
    }

    public SELF assertArrayProperty(Consumer<List> assertProperties, String propertyJsonPath) {
      List properties = response.then().extract().path(propertyJsonPath);
      assertProperties.accept(properties);
      return (SELF) this;
    }

    /**
     * special assertion of the status code
     *
     * @param expectedStatusCode the expected status code
     * @return the self object
     */
    public SELF assertStatusCode(int expectedStatusCode) {
      this.response.then().assertThat().statusCode(expectedStatusCode);
      return (SELF) this;
    }
  }

  public class AutoCompleteResponse<PREV extends ModelResponse> extends ModelResponse<AutoCompleteResponse<PREV>, PREV> {

    public AutoCompleteResponse(Response response, PREV previousResponse) {
      super(response, previousResponse);
    }

    public AutoCompleteResponse<PREV> assertAutoCompleteResults(Consumer<List<Map>> checker) {
      List<Map> result = response.then().extract().path("");
      checker.accept(result);
      return this;
    }

  }


  public class DiffResponse<PREV extends ModelResponse> extends ModelResponse<DiffResponse<PREV>, PREV> {

    public DiffResponse(Response response, PREV previousResponse) {
      super(response, previousResponse);
    }
  }

  public class ChangePasswordResponse<PREV extends ModelResponse> extends ModelResponse<ChangePasswordResponse<PREV>, PREV> {

    public ChangePasswordResponse(Response response, PREV previousResponse) {
      super(response, previousResponse);
    }
  }
}
