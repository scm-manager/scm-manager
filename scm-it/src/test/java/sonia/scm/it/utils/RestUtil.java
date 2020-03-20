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

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

import java.net.URI;

import static java.net.URI.create;

public class RestUtil {

  public static final URI BASE_URL = create("http://localhost:8081/scm/");
  public static final URI REST_BASE_URL = BASE_URL.resolve("api/v2/");

  public static URI createResourceUrl(String path) {
    return REST_BASE_URL.resolve(path);
  }

  public static final String ADMIN_USERNAME = "scmadmin";
  public static final String ADMIN_PASSWORD = "scmadmin";

  public static RequestSpecification given() {
    return RestAssured.given()
      .auth().preemptive().basic(ADMIN_USERNAME, ADMIN_PASSWORD);
  }

  public static RequestSpecification given(String mediaType) {
    return given(mediaType, ADMIN_USERNAME, ADMIN_PASSWORD);
  }

  public static RequestSpecification given(String mediaType, String username, String password) {
    return givenAnonymous(mediaType)
      .auth().preemptive().basic(username, password);
  }

  public static RequestSpecification givenAnonymous(String mediaType) {
    return RestAssured.given()
      .contentType(mediaType)
      .accept(mediaType);
  }
}
