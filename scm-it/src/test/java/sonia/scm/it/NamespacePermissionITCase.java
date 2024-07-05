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

package sonia.scm.it;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import junit.framework.AssertionFailedError;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.it.webapp.ScmClient;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static sonia.scm.it.utils.RestUtil.createResourceUrl;
import static sonia.scm.it.utils.RestUtil.given;
import static sonia.scm.it.utils.TestData.assignPermissions;
import static sonia.scm.it.utils.TestData.cleanup;
import static sonia.scm.it.utils.TestData.createUser;
import static sonia.scm.it.webapp.IntegrationTestUtil.createAdminClient;
import static sonia.scm.it.webapp.IntegrationTestUtil.createResource;
import static sonia.scm.it.webapp.RepositoryITUtil.createRepository;
import static sonia.scm.it.webapp.RepositoryITUtil.setNamespaceStrategy;

public class NamespacePermissionITCase {

  @Before
  public void setUp() {
  }

  @Test
  public void shouldKeepNamespacePermissionsForExistingNamespace() {
    cleanup();

    // let admin create repository in namespace 'existing'
    ScmClient adminApiClient = createAdminClient();
    setNamespaceStrategy(adminApiClient, "CustomNamespaceStrategy");

    Response response =
      createResource(adminApiClient, "repositories")
        .accept("*/*")
        .post(Entity.entity("""
          {
                   "contact": "zaphod.beeblebrox@hitchhiker.com",
                   "description": "Heart of Gold is the first prototype ship to successfully utilise the revolutionary Infinite Improbability Drive",
                   "namespace": "existing",
                   "name": "HeartOfGold-git",
                   "archived": false,
                   "type": "git"
                 }
          """, MediaType.valueOf(VndMediaType.REPOSITORY)));

    assertNotNull(response);
    assertEquals(201, response.getStatus());

    URI url = URI.create(response.getHeaders().get("Location").get(0).toString());
    response.close();

    // create user with 'create repositories' permission only
    createUser("dent", "dent", false, "xml", "arthur@example.com");
    assignPermissions("dent", "repository:create");

    // let new user create a new repository in the existing namespace
    ScmClient userClient = new ScmClient("dent", "dent");
    createRepository(userClient, """
      {
               "contact": "dent@hitchhiker.com",
               "description": "I want it all",
               "namespace": "existing",
               "name": "Earth",
               "archived": false,
               "type": "git"
             }
      """, "CustomNamespaceStrategy");

    // user should not have permissions on namespace, only on new repository
    given(VndMediaType.REPOSITORY, "dent", "dent")

      .when()
      .get(url)

      .then()
      .statusCode(403);

    // user should have no permissions in namespace
    String permissionUrl = getNamespacePermissionUrl("existing");
    Map<String, String> permissions = given()
      .when()
      .get(permissionUrl)
      .then()
      .statusCode(200)
      .extract()
      .body().jsonPath().getList("_embedded.permissions")
      .stream()
      .collect(Collectors.toMap(
        e -> ((Map) e).get("name").toString(),
        e -> ((Map) e).get("role").toString()
      ));

    assertNull(permissions.get("dent"));
    assertEquals("OWNER", permissions.get("scmadmin"));
  }

  @Test
  public void shouldCreateNamespacePermissionsForNewNamespace() {
    cleanup();

    ScmClient adminApiClient = createAdminClient();
    setNamespaceStrategy(adminApiClient, "CustomNamespaceStrategy");

    // create user with 'create repositories' permission only
    createUser("dent", "dent", false, "xml", "arthur@example.com");
    assignPermissions("dent", "repository:create");

    // let new user create a new repository in new namespace
    ScmClient userClient = new ScmClient("dent", "dent");
    createRepository(userClient, """
      {
               "contact": "dent@hitchhiker.com",
               "description": "I want it all",
               "namespace": "new",
               "name": "Earth",
               "archived": false,
               "type": "git"
             }
      """, "CustomNamespaceStrategy");

    // user should have OWNER permissions in namespace
    String permissionUrl = getNamespacePermissionUrl("new");
    Map<String, String> permissions = given()
      .when()
      .get(permissionUrl)
      .then()
      .statusCode(200)
      .extract()
      .body().jsonPath().getList("_embedded.permissions")
      .stream()
      .collect(Collectors.toMap(
        e -> ((Map) e).get("name").toString(),
        e -> ((Map) e).get("role").toString()
      ));

    assertEquals("OWNER", permissions.get("dent"));
  }

  private String getNamespacePermissionUrl(String namespace) {
    List<Map<String, Object>> namespaces = given(VndMediaType.NAMESPACE_COLLECTION)
      .when()
      .get(createResourceUrl("namespaces"))
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .body().jsonPath().getList("_embedded.namespaces");
    return (
      (Map<String, Object>) (
        (Map<String, Object>) namespaces
          .stream()
          .filter(m -> m.get("namespace").equals(namespace))
          .findFirst()
          .orElseThrow(() -> new AssertionFailedError("namespace not found: " + namespace))
          .get("_links"))
        .get("permissions"))
      .get("href").toString();
  }
}
