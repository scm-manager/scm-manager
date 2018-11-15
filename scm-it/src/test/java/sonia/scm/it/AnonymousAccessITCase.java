package sonia.scm.it;

import io.restassured.RestAssured;
import org.junit.Test;
import sonia.scm.it.utils.RestUtil;
import sonia.scm.it.utils.ScmRequests;

import static org.junit.Assert.assertEquals;

public class AnonymousAccessITCase {

  @Test
  public void shouldAccessIndexResourceWithoutAuthentication() {
    ScmRequests.start()
      .requestIndexResource()
      .assertStatusCode(200);
  }

  @Test
  public void shouldRejectUserResourceWithoutAuthentication() {
    assertEquals(401, RestAssured.given()
      .when()
      .get(RestUtil.REST_BASE_URL.resolve("users/"))
      .statusCode());
  }
}
