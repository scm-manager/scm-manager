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

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import sonia.scm.it.utils.RepositoryUtil;
import sonia.scm.it.utils.RestUtil;
import sonia.scm.it.utils.ScmRequests;
import sonia.scm.it.utils.ScmTypes;
import sonia.scm.it.utils.TestData;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.repository.client.api.RepositoryClientException;
import sonia.scm.security.AnonymousMode;

import javax.json.Json;
import javax.json.JsonArray;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sonia.scm.it.utils.TestData.JSON_BUILDER;
import static sonia.scm.it.utils.TestData.USER_ANONYMOUS;
import static sonia.scm.it.utils.TestData.WRITE;
import static sonia.scm.it.utils.TestData.getDefaultRepositoryUrl;

class AnonymousAccessITCase {

  @BeforeEach
  void createRepositoryAndSetAnonymous() {
    TestData.createDefault();
  }

  @Test
  void shouldAccessIndexResourceWithoutAuthentication() {
    ScmRequests.start()
      .requestIndexResource()
      .assertStatusCode(200);
  }

  @Test
  void shouldRejectRepositoryResourceWithoutAuthentication() {
    assertEquals(401, RestAssured.given()
      .when()
      .get(RestUtil.REST_BASE_URL.resolve("repositories/"))
      .statusCode());
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class WithProtocolOnlyAnonymousAccess {

    @BeforeEach
    void createRepositoryAndSetAnonymous() {
      setAnonymousAccess(AnonymousMode.PROTOCOL_ONLY);
    }

    @Test
    void shouldGrantAnonymousAccessToRepositoryList() {
      assertEquals(200, RestAssured.given()
        .when()
        .get(RestUtil.REST_BASE_URL.resolve("repositories"))
        .statusCode());
    }

    @Nested
    class WithoutAnonymousAccessForRepository {

      @ParameterizedTest
      @ArgumentsSource(ScmTypes.class)
      void shouldGrantAnonymousAccessToRepository(String type) {
        assertEquals(401, RestAssured.given()
          .when()
          .get(getDefaultRepositoryUrl(type))
          .statusCode());
      }

      @ParameterizedTest
      @ArgumentsSource(ScmTypes.class)
      void shouldNotCloneRepository(String type, @TempDir Path temporaryFolder) {
        assertThrows(RepositoryClientException.class, () -> RepositoryUtil.createAnonymousRepositoryClient(type, Files.createDirectories(temporaryFolder).toFile()));
      }
    }

    @Nested
    class WithAnonymousAccessForRepository {

      @BeforeEach
      void grantAnonymousAccessToRepo() {
        ScmTypes.availableScmTypes().forEach(type -> TestData.createUserPermission(USER_ANONYMOUS, WRITE, type));
      }

      @ParameterizedTest
      @ArgumentsSource(ScmTypes.class)
      void shouldGrantAnonymousAccessToRepository(String type) {
        assertEquals(200, RestAssured.given()
          .when()
          .get(getDefaultRepositoryUrl(type))
          .statusCode());
      }

      @ParameterizedTest
      @ArgumentsSource(ScmTypes.class)
      void shouldCloneRepository(String type, @TempDir Path temporaryFolder) throws IOException {
        RepositoryClient client = RepositoryUtil.createAnonymousRepositoryClient(type, Files.createDirectories(temporaryFolder).toFile());
        assertEquals(1, Objects.requireNonNull(client.getWorkingCopy().list()).length);
      }
    }

    @AfterAll
    void disableAnonymousAccess() {
      setAnonymousAccess(AnonymousMode.OFF);
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class WithFullAnonymousAccess {

    @BeforeEach
    void createRepositoryAndSetAnonymous() {
      setAnonymousAccess(AnonymousMode.FULL);
    }

    @Test
    void shouldGrantAnonymousAccessToRepositoryList() {
      assertEquals(200, RestAssured.given()
        .when()
        .get(RestUtil.REST_BASE_URL.resolve("repositories"))
        .statusCode());
    }

    @Nested
    class WithoutAnonymousAccessForRepository {

      @ParameterizedTest
      @ArgumentsSource(ScmTypes.class)
      void shouldGrantAnonymousAccessToRepository(String type) {
        assertEquals(401, RestAssured.given()
          .when()
          .get(getDefaultRepositoryUrl(type))
          .statusCode());
      }

      @ParameterizedTest
      @ArgumentsSource(ScmTypes.class)
      void shouldNotCloneRepository(String type, @TempDir Path temporaryFolder) {
        assertThrows(RepositoryClientException.class, () -> RepositoryUtil.createAnonymousRepositoryClient(type, Files.createDirectories(temporaryFolder).toFile()));
      }
    }

    @Nested
    class WithAnonymousAccessForRepository {

      @BeforeEach
      void grantAnonymousAccessToRepo() {
        ScmTypes.availableScmTypes().forEach(type -> TestData.createUserPermission(USER_ANONYMOUS, WRITE, type));
      }

      @ParameterizedTest
      @ArgumentsSource(ScmTypes.class)
      void shouldGrantAnonymousAccessToRepository(String type) {
        assertEquals(200, RestAssured.given()
          .when()
          .get(getDefaultRepositoryUrl(type))
          .statusCode());
      }

      @ParameterizedTest
      @ArgumentsSource(ScmTypes.class)
      void shouldCloneRepository(String type, @TempDir Path temporaryFolder) throws IOException {
        RepositoryClient client = RepositoryUtil.createAnonymousRepositoryClient(type, Files.createDirectories(temporaryFolder).toFile());
        assertEquals(1, Objects.requireNonNull(client.getWorkingCopy().list()).length);
      }
    }

    @AfterAll
    void disableAnonymousAccess() {
      setAnonymousAccess(AnonymousMode.OFF);
    }
  }

  private static void setAnonymousAccess(AnonymousMode anonymousMode) {
    RestUtil.given("application/vnd.scmm-config+json;v=2")
      .body(createConfig(anonymousMode))

      .when()
      .put(RestUtil.REST_BASE_URL.toASCIIString() + "config")

      .then()
      .statusCode(HttpServletResponse.SC_NO_CONTENT);
  }

  private static String createConfig(AnonymousMode anonymousMode) {
    JsonArray emptyArray = Json.createBuilderFactory(emptyMap()).createArrayBuilder().build();
    return JSON_BUILDER
      .add("adminGroups", emptyArray)
      .add("adminUsers", emptyArray)
      .add("anonymousMode", anonymousMode.toString())
      .add("baseUrl", "https://next-scm.cloudogu.com/scm")
      .add("dateFormat", "YYYY-MM-DD HH:mm:ss")
      .add("disableGroupingGrid", false)
      .add("enableProxy", false)
      .add("enabledXsrfProtection", true)
      .add("forceBaseUrl", false)
      .add("loginAttemptLimit", 100)
      .add("loginAttemptLimitTimeout", 300)
      .add("loginInfoUrl", "https://login-info.scm-manager.org/api/v1/login-info")
      .add("namespaceStrategy", "UsernameNamespaceStrategy")
      .add("pluginUrl", "https://oss.cloudogu.com/jenkins/job/scm-manager/job/scm-manager-bitbucket/job/plugin-snapshot/job/master/lastSuccessfulBuild/artifact/plugins/plugin-center.json")
      .add("proxyExcludes", emptyArray)
      .addNull("proxyPassword")
      .add("proxyPort", 8080)
      .add("proxyServer", "proxy.mydomain.com")
      .addNull("proxyUser")
      .add("realmDescription", "SONIA :: SCM Manager")
      .add("skipFailedAuthenticators", false)
      .build().toString();
  }
}
