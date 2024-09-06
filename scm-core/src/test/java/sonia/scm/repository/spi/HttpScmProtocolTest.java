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
