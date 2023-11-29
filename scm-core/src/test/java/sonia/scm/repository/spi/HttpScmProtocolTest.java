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

package sonia.scm.repository.spi;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import sonia.scm.repository.Repository;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HttpScmProtocolTest {

  private String namespace;
  private String name;

  @Nested
  class WithSimpleNamespaceAndName {

    @BeforeEach
    void setNamespaceAndName() {
      namespace = "space";
      name = "name";
    }

    @TestFactory
    Stream<DynamicTest> shouldCreateCorrectUrlsWithContextPath() {
      return Stream.of("http://localhost/scm", "http://localhost/scm/")
        .map(url -> assertResultingUrl(url, "http://localhost/scm/repo/space/name"));
    }

    @TestFactory
    Stream<DynamicTest> shouldCreateCorrectUrlsWithPort() {
      return Stream.of("http://localhost:8080", "http://localhost:8080/")
        .map(url -> assertResultingUrl(url, "http://localhost:8080/repo/space/name"));
    }
  }

  @Nested
  class WithComplexNamespaceAndName{

    @BeforeEach
    void setNamespaceAndName() {
      namespace = "name space";
      name = "name";
    }

    @Test
    void shouldCreateCorrectUrlsWithContextPath() {
      assertResultingUrl("http://localhost/scm", "http://localhost/scm/repo/name%20space/name");
    }
  }

  DynamicTest assertResultingUrl(String baseUrl, String expectedUrl) {
    String actualUrl = createInstanceOfHttpScmProtocol(baseUrl).getUrl();
    return DynamicTest.dynamicTest(baseUrl + " -> " + expectedUrl, () -> assertThat(actualUrl).isEqualTo(expectedUrl));
  }

  private HttpScmProtocol createInstanceOfHttpScmProtocol(String baseUrl) {
    return new HttpScmProtocol(new Repository("", "", namespace, name), baseUrl) {
      @Override
      protected void serve(HttpServletRequest request, HttpServletResponse response, Repository repository, ServletConfig config) {
      }
    };
  }
}
