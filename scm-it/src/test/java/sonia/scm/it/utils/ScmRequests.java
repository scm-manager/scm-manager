package sonia.scm.it.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
public class ScmRequests {

  private static final Logger LOG = LoggerFactory.getLogger(ScmRequests.class);

  private String url;
  private String username;
  private String password;

  public static ScmRequests start() {
    return new ScmRequests();
  }

  public Given given() {
    return new Given();
  }


  /**
   * Apply a GET Request to the extracted url from the given link
   *
   * @param linkPropertyName the property name of link
   * @param response         the response containing the link
   * @return the response of the GET request using the given link
   */
  private Response applyGETRequestFromLink(Response response, String linkPropertyName) {
    String result = response
      .then()
      .extract()
      .path(linkPropertyName);
    LOG.info("Extracted result {} from response: {}", linkPropertyName, result);
    return applyGETRequest(result);
  }


  /**
   * Apply a GET Request to the given <code>url</code> and return the response.
   *
   * @param url the url of the GET request
   * @return the response of the GET request using the given <code>url</code>
   */
  private Response applyGETRequest(String url) {
    return RestAssured.given()
      .auth().preemptive().basic(username, password)
      .when()
      .get(url);
  }


  /**
   * Apply a PUT Request to the extracted url from the given link
   *
   * @param response         the response containing the link
   * @param linkPropertyName the property name of link
   * @param body
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
   * @param url       the url of the PUT request
   * @param mediaType
   * @param body
   * @return the response of the PUT request using the given <code>url</code>
   */
  private Response applyPUTRequest(String url, String mediaType, String body) {
    return RestAssured.given()
      .auth().preemptive().basic(username, password)
      .when()
      .contentType(mediaType)
      .accept(mediaType)
      .body(body)
      .put(url);
  }


  private void setUrl(String url) {
    this.url = url;
  }

  private void setUsername(String username) {
    this.username = username;
  }

  private void setPassword(String password) {
    this.password = password;
  }

  private String getUrl() {
    return url;
  }

  private String getUsername() {
    return username;
  }

  private String getPassword() {
    return password;
  }

  public class Given {

    public GivenUrl url(String url) {
      setUrl(url);
      return new GivenUrl();
    }

    public GivenUrl url(URI url) {
      setUrl(url.toString());
      return new GivenUrl();
    }

  }

  public class GivenWithUrlAndAuth {
    public AppliedMeRequest getMeResource() {
      return new AppliedMeRequest(applyGETRequest(url));
    }

    public AppliedUserRequest getUserResource() {
      return new AppliedUserRequest(applyGETRequest(url));
    }

    public AppliedRepositoryRequest getRepositoryResource() {
      return new AppliedRepositoryRequest(
        applyGETRequest(url)
      );
    }
  }

  public class AppliedRequest<SELF extends AppliedRequest> {
    private Response response;

    public AppliedRequest(Response response) {
      this.response = response;
    }

    /**
     * apply custom assertions to the actual response
     *
     * @param consumer consume the response in order to assert the content. the header, the payload etc..
     * @return the self object
     */
    public SELF assertResponse(Consumer<Response> consumer) {
      consumer.accept(response);
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

  public class AppliedRepositoryRequest extends AppliedRequest<AppliedRepositoryRequest> {

    public AppliedRepositoryRequest(Response response) {
      super(response);
    }

    public RepositoryResponse usingRepositoryResponse() {
      return new RepositoryResponse(super.response);
    }
  }

  public class RepositoryResponse {

    private Response repositoryResponse;

    public RepositoryResponse(Response repositoryResponse) {
      this.repositoryResponse = repositoryResponse;
    }

    public AppliedSourcesRequest requestSources() {
      return new AppliedSourcesRequest(applyGETRequestFromLink(repositoryResponse, "_links.sources.href"));
    }

    public AppliedChangesetsRequest requestChangesets() {
      return new AppliedChangesetsRequest(applyGETRequestFromLink(repositoryResponse, "_links.changesets.href"));
    }
  }

  public class AppliedChangesetsRequest extends AppliedRequest<AppliedChangesetsRequest> {

    public AppliedChangesetsRequest(Response response) {
      super(response);
    }

    public ChangesetsResponse usingChangesetsResponse() {
      return new ChangesetsResponse(super.response);
    }
  }

  public class ChangesetsResponse {
    private Response changesetsResponse;

    public ChangesetsResponse(Response changesetsResponse) {
      this.changesetsResponse = changesetsResponse;
    }

    public ChangesetsResponse assertChangesets(Consumer<List<Map>> changesetsConsumer) {
      List<Map> changesets = changesetsResponse.then().extract().path("_embedded.changesets");
      changesetsConsumer.accept(changesets);
      return this;
    }

    public AppliedDiffRequest requestDiff(String revision) {
      return new AppliedDiffRequest(applyGETRequestFromLink(changesetsResponse, "_embedded.changesets.find{it.id=='" + revision + "'}._links.diff.href"));
    }

    public AppliedModificationsRequest requestModifications(String revision) {
      return new AppliedModificationsRequest(applyGETRequestFromLink(changesetsResponse, "_embedded.changesets.find{it.id=='" + revision + "'}._links.modifications.href"));
    }
  }

  public class AppliedSourcesRequest extends AppliedRequest<AppliedSourcesRequest> {

    public AppliedSourcesRequest(Response sourcesResponse) {
      super(sourcesResponse);
    }

    public SourcesResponse usingSourcesResponse() {
      return new SourcesResponse(super.response);
    }
  }

  public class SourcesResponse {

    private Response sourcesResponse;

    public SourcesResponse(Response sourcesResponse) {
      this.sourcesResponse = sourcesResponse;
    }

    public AppliedChangesetsRequest requestFileHistory(String fileName) {
      return new AppliedChangesetsRequest(applyGETRequestFromLink(sourcesResponse, "_embedded.children.find{it.name=='" + fileName + "'}._links.history.href"));
    }

    public AppliedSourcesRequest requestSelf(String fileName) {
      return new AppliedSourcesRequest(applyGETRequestFromLink(sourcesResponse, "_embedded.children.find{it.name=='" + fileName + "'}._links.self.href"));
    }
  }

  public class AppliedDiffRequest extends AppliedRequest<AppliedDiffRequest> {

    public AppliedDiffRequest(Response response) {
      super(response);
    }
  }

  public class GivenUrl {

    public GivenWithUrlAndAuth usernameAndPassword(String username, String password) {
      setUsername(username);
      setPassword(password);
      return new GivenWithUrlAndAuth();
    }
  }

  public class AppliedModificationsRequest extends AppliedRequest<AppliedModificationsRequest> {
    public AppliedModificationsRequest(Response response) {
      super(response);
    }

    public ModificationsResponse usingModificationsResponse() {
      return new ModificationsResponse(super.response);
    }

  }

  public class ModificationsResponse {
    private Response resource;

    public ModificationsResponse(Response resource) {
      this.resource = resource;
    }

    public ModificationsResponse assertRevision(Consumer<String> assertRevision) {
      String revision = resource.then().extract().path("revision");
      assertRevision.accept(revision);
      return this;
    }

    public ModificationsResponse assertAdded(Consumer<List<String>> assertAdded) {
      List<String> added = resource.then().extract().path("added");
      assertAdded.accept(added);
      return this;
    }

    public ModificationsResponse assertRemoved(Consumer<List<String>> assertRemoved) {
      List<String> removed = resource.then().extract().path("removed");
      assertRemoved.accept(removed);
      return this;
    }

    public ModificationsResponse assertModified(Consumer<List<String>> assertModified) {
      List<String> modified = resource.then().extract().path("modified");
      assertModified.accept(modified);
      return this;
    }

  }

  public class AppliedMeRequest extends AppliedRequest<AppliedMeRequest> {

    public AppliedMeRequest(Response response) {
      super(response);
    }

    public MeResponse usingMeResponse() {
      return new MeResponse(super.response);
    }

  }

  public class MeResponse extends UserResponse<MeResponse> {


    public MeResponse(Response response) {
      super(response);
    }

    public AppliedChangePasswordRequest requestChangePassword(String oldPassword, String newPassword) {
      return new AppliedChangePasswordRequest(applyPUTRequestFromLink(super.response, "_links.password.href", VndMediaType.PASSWORD_CHANGE, createPasswordChangeJson(oldPassword, newPassword)));
    }


  }

  public class UserResponse<SELF extends UserResponse> extends ModelResponse<SELF> {

    public static final String LINKS_PASSWORD_HREF = "_links.password.href";

    public UserResponse(Response response) {
      super(response);
    }

    public SELF assertPassword(Consumer<String> assertPassword) {
      return super.assertSingleProperty(assertPassword, "password");
    }

    public SELF assertType(Consumer<String> assertType) {
      return assertSingleProperty(assertType, "type");
    }

    public SELF assertAdmin(Consumer<Boolean> assertAdmin) {
      return assertSingleProperty(assertAdmin, "admin");
    }

    public SELF assertPasswordLinkDoesNotExists() {
      return assertPropertyPathDoesNotExists(LINKS_PASSWORD_HREF);
    }

    public SELF assertPasswordLinkExists() {
      return assertPropertyPathExists(LINKS_PASSWORD_HREF);
    }

    public AppliedChangePasswordRequest requestChangePassword(String newPassword) {
      return new AppliedChangePasswordRequest(applyPUTRequestFromLink(super.response, LINKS_PASSWORD_HREF, VndMediaType.PASSWORD_CHANGE, createPasswordChangeJson(null, newPassword)));
    }

  }


  /**
   * encapsulate standard assertions over model properties
   */
  public class ModelResponse<SELF extends ModelResponse> {

    protected Response response;

    public ModelResponse(Response response) {
      this.response = response;
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
  }

  public class AppliedChangePasswordRequest extends AppliedRequest<AppliedChangePasswordRequest> {

    public AppliedChangePasswordRequest(Response response) {
      super(response);
    }

  }

  public class AppliedUserRequest extends AppliedRequest<AppliedUserRequest> {

    public AppliedUserRequest(Response response) {
      super(response);
    }

    public UserResponse usingUserResponse() {
      return new UserResponse(super.response);
    }

  }
}
