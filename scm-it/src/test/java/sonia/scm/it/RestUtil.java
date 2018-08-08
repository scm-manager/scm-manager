package sonia.scm.it;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

import java.net.URI;

import static java.net.URI.create;

public class RestUtil {

  public static final URI BASE_URL = create("http://localhost:8081/scm/");
  public static final URI REST_BASE_URL = BASE_URL.resolve("api/rest/v2/");

  public static URI createResourceUrl(String path) {
    return REST_BASE_URL.resolve(path);
  }

  public static RequestSpecification given(String mediaType) {
    return RestAssured.given()
      .contentType(mediaType)
      .accept(mediaType)
      .auth().preemptive().basic("scmadmin", "scmadmin");
  }
}
