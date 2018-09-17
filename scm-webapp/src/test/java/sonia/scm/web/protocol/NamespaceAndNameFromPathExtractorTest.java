package sonia.scm.web.protocol;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import sonia.scm.repository.NamespaceAndName;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class NamespaceAndNameFromPathExtractorTest {
  @TestFactory
  Stream<DynamicNode> shouldExtractCorrectNamespaceAndName() {
    return Stream.of(
      "/space/repo",
      "/space/repo/",
      "/space/repo/here",
      "/space/repo/here/there",
      "space/repo",
      "space/repo/",
      "space/repo/here/there"
    ).map(this::createCorrectTest);
  }

  @TestFactory
  Stream<DynamicNode> shouldHandleTrailingDotSomethings() {
    return Stream.of(
      "/space/repo.git",
      "/space/repo.and.more",
      "/space/repo."
    ).map(this::createCorrectTest);
  }

  private DynamicTest createCorrectTest(String path) {
    return dynamicTest(
      "should extract correct namespace and name for path " + path,
      () -> {
        Optional<NamespaceAndName> namespaceAndName = NamespaceAndNameFromPathExtractor.fromUri(path);

        assertThat(namespaceAndName.get()).isEqualTo(new NamespaceAndName("space", "repo"));
      }
    );
  }

  @TestFactory
  Stream<DynamicNode> shouldHandleMissingParts() {
    return Stream.of(
      "",
      "/",
      "/space",
      "/space/"
    ).map(this::createFailureTest);
  }

  private DynamicTest createFailureTest(String path) {
    return dynamicTest(
      "should not fail for wrong path " + path,
      () -> {
        Optional<NamespaceAndName> namespaceAndName = NamespaceAndNameFromPathExtractor.fromUri(path);

        assertThat(namespaceAndName.isPresent()).isFalse();
      }
    );
  }
}
