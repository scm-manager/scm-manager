package sonia.scm.it;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


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
public class RepositoryRequests {

  private String url;
  private String username;
  private String password;

  static RepositoryRequests start() {
    return new RepositoryRequests();
  }

  Given given() {
    return new Given();
  }


  /**
   * Apply a GET Request to the extracted url from the given link
   *
   * @param linkPropertyName the property name of link
   * @param response         the response containing the link
   * @return the response of the GET request using the given link
   */
  private Response getResponseFromLink(Response response, String linkPropertyName) {
    return getResponse(response
      .then()
      .extract()
      .path(linkPropertyName));
  }


  /**
   * Apply a GET Request to the given <code>url</code> and return the response.
   *
   * @param url the url of the GET request
   * @return the response of the GET request using the given <code>url</code>
   */
  private Response getResponse(String url) {
    return RestAssured.given()
      .auth().preemptive().basic(username, password)
      .when()
      .get(url);
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

  class Given {

    GivenUrl url(String url) {
      setUrl(url);
      return new GivenUrl();
    }

  }

  class GivenWithUrlAndAuth {
    AppliedRepositoryGetRequest get() {
      return new AppliedRepositoryGetRequest(
        getResponse(url)
      );
    }
  }

  class AppliedGetRequest<SELF extends AppliedGetRequest> {
    private Response response;

    public AppliedGetRequest(Response response) {
      this.response = response;
    }

    /**
     * apply custom assertions to the actual response
     *
     * @param consumer consume the response in order to assert the content. the header, the payload etc..
     * @return the self object
     */
    SELF assertResponse(Consumer<Response> consumer) {
      consumer.accept(response);
      return (SELF) this;
    }

    /**
     * special assertion of the status code
     *
     * @param expectedStatusCode the expected status code
     * @return the self object
     */
    SELF assertStatusCode(int expectedStatusCode) {
      this.response.then().assertThat().statusCode(expectedStatusCode);
      return (SELF) this;
    }

  }

  class AppliedRepositoryGetRequest extends AppliedGetRequest<AppliedRepositoryGetRequest> {

    AppliedRepositoryGetRequest(Response response) {
      super(response);
    }

    RepositoryResponse usingRepositoryResponse() {
      return new RepositoryResponse(super.response);
    }
  }

  class RepositoryResponse {

    private Response repositoryResponse;

    public RepositoryResponse(Response repositoryResponse) {
      this.repositoryResponse = repositoryResponse;
    }

    AppliedGetSourcesRequest requestSources() {
      return new AppliedGetSourcesRequest(getResponseFromLink(repositoryResponse, "_links.sources.href"));
    }

    AppliedGetChangesetsRequest requestChangesets(String fileName) {
      return new AppliedGetChangesetsRequest(getResponseFromLink(repositoryResponse, "_links.changesets.href"));
    }
  }

  class AppliedGetChangesetsRequest extends AppliedGetRequest<AppliedGetChangesetsRequest> {

    AppliedGetChangesetsRequest(Response response) {
      super(response);
    }

    ChangesetsResponse usingChangesetsResponse() {
      return new ChangesetsResponse(super.response);
    }
  }

  class ChangesetsResponse {
    private Response changesetsResponse;

    public ChangesetsResponse(Response changesetsResponse) {
      this.changesetsResponse = changesetsResponse;
    }

    ChangesetsResponse assertChangesets(Consumer<List<Map>> changesetsConsumer) {
      List<Map> changesets = changesetsResponse.then().extract().path("_embedded.changesets");
      changesetsConsumer.accept(changesets);
      return this;
    }

    AppliedGetDiffRequest requestDiff(String revision) {
      return new AppliedGetDiffRequest(getResponseFromLink(changesetsResponse, "_embedded.changesets.find{it.id=='" + revision + "'}._links.diff.href"));
    }

  }

  class AppliedGetSourcesRequest extends AppliedGetRequest<AppliedGetSourcesRequest> {

    public AppliedGetSourcesRequest(Response sourcesResponse) {
      super(sourcesResponse);
    }

    SourcesResponse usingSourcesResponse() {
      return new SourcesResponse(super.response);
    }
  }

  class SourcesResponse {

    private Response sourcesResponse;

    SourcesResponse(Response sourcesResponse) {
      this.sourcesResponse = sourcesResponse;
    }

    SourcesResponse assertRevision(Consumer<String> assertRevision) {
      String revision = sourcesResponse.then().extract().path("revision");
      assertRevision.accept(revision);
      return this;
    }

    SourcesResponse assertFiles(Consumer<List> assertFiles) {
      List files = sourcesResponse.then().extract().path("files");
      assertFiles.accept(files);
      return this;
    }

    AppliedGetChangesetsRequest requestFileHistory(String fileName) {
      return new AppliedGetChangesetsRequest(getResponseFromLink(sourcesResponse, "_embedded.files.find{it.name=='" + fileName + "'}._links.history.href"));
    }

    AppliedGetSourcesRequest requestSelf(String fileName) {
      return new AppliedGetSourcesRequest(getResponseFromLink(sourcesResponse, "_embedded.files.find{it.name=='" + fileName + "'}._links.self.href"));
    }
  }

  class AppliedGetDiffRequest extends AppliedGetRequest<AppliedGetDiffRequest> {

    AppliedGetDiffRequest(Response response) {
      super(response);
    }
  }

  class GivenUrl {

    GivenWithUrlAndAuth usernameAndPassword(String username, String password) {
      setUsername(username);
      setPassword(password);
      return new GivenWithUrlAndAuth();
    }
  }
}
