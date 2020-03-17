/**
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
package sonia.scm.web.protocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NamespaceAndNameFromPathExtractorTest {

  @Mock
  private RepositoryManager repositoryManager;

  private NamespaceAndNameFromPathExtractor extractor;

  @BeforeEach
  void setUpObjectUnderTest() {
    List<RepositoryType> types = Arrays.asList(
      new RepositoryType("git", "Git", Collections.emptySet()),
      new RepositoryType("hg", "Mercurial", Collections.emptySet()),
      new RepositoryType("svn", "Subversion", Collections.emptySet())
    );
    when(repositoryManager.getConfiguredTypes()).thenReturn(types);
    extractor = new NamespaceAndNameFromPathExtractor(repositoryManager);
  }

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
  Stream<DynamicNode> shouldHandleTypeSuffix() {
    return Stream.of(
      "/space/repo.git",
      "/space/repo.hg",
      "/space/repo.svn",
      "/space/repo"
    ).map(this::createCorrectTest);
  }

  private DynamicTest createCorrectTest(String path) {
    return createCorrectTest(path, new NamespaceAndName("space", "repo"));
  }

  private DynamicTest createCorrectTest(String path, NamespaceAndName expected) {
    return dynamicTest(
      "should extract correct namespace and name for path " + path,
      () -> {
        Optional<NamespaceAndName> namespaceAndName = extractor.fromUri(path);

        assertThat(namespaceAndName.get()).isEqualTo(expected);
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
        Optional<NamespaceAndName> namespaceAndName = extractor.fromUri(path);

        assertThat(namespaceAndName.isPresent()).isFalse();
      }
    );
  }

  @TestFactory
  Stream<DynamicNode> shouldHandleDots() {
    return Stream.of(
      "/space/repo.with.dots.git",
      "/space/repo.with.dots.hg",
      "/space/repo.with.dots.svn",
      "/space/repo.with.dots"
    ).map(path -> createCorrectTest(path, new NamespaceAndName("space", "repo.with.dots")));
  }

  @Test
  void shouldNotFailOnEndingDot() {
    Optional<NamespaceAndName> namespaceAndName = extractor.fromUri("/space/repo.");
    assertThat(namespaceAndName).contains(new NamespaceAndName("space", "repo."));
  }
}
