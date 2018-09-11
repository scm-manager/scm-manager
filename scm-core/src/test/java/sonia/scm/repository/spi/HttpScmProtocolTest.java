package sonia.scm.repository.spi;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import sonia.scm.repository.Repository;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HttpScmProtocolTest {

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

  DynamicTest assertResultingUrl(String baseUrl, String expectedUrl) {
    String actualUrl = createInstanceOfHttpScmProtocol(baseUrl).getUrl();
    return DynamicTest.dynamicTest(baseUrl + " -> " + expectedUrl, () -> assertThat(actualUrl).isEqualTo(expectedUrl));
  }

  private HttpScmProtocol createInstanceOfHttpScmProtocol(String baseUrl) {
    return new HttpScmProtocol(new Repository("", "", "space", "name"), baseUrl) {
      @Override
      protected void serve(HttpServletRequest request, HttpServletResponse response, Repository repository, ServletConfig config) {
      }
    };
  }
}
