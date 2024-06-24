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

import org.eclipse.jgit.attributes.Attribute;
import org.eclipse.jgit.attributes.Attributes;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AttributeAnalyzerTest extends AbstractGitCommandTestBase {

  private AttributeAnalyzer attributeAnalyzer;

  @Before
  public void initAnalyzer() {
    attributeAnalyzer = new AttributeAnalyzer(createContext(), new GitModificationsCommand(createContext()));
  }

  @Test
  public void shouldNotGetAttributesIfFileDoesNotExist() throws IOException {
    RevCommit commit = attributeAnalyzer.getTargetCommit("main");
    Optional<Attributes> attributes = attributeAnalyzer.getAttributes(commit,"text1234.txt");

    assertThat(attributes).isNotPresent();
  }

  @Test
  public void shouldNotGetAttributesIfDoesNotExistForFilePattern() throws IOException {
    RevCommit commit = attributeAnalyzer.getTargetCommit("main");
    Optional<Attributes> attributes = attributeAnalyzer.getAttributes(commit,"text.txt");

    assertThat(attributes).isNotPresent();
  }

  @Test
  public void shouldGetAttributes() throws IOException {
    RevCommit commit = attributeAnalyzer.getTargetCommit("main");
    Optional<Attributes> attributes = attributeAnalyzer.getAttributes(commit,"text.ipr");

    assertThat(attributes).isPresent();
    Collection<Attribute> attributeCollection = attributes.get().getAll();
    Attribute firstAttribute = attributeCollection.iterator().next();
    assertThat(firstAttribute.getKey()).isEqualTo("merge");
    assertThat(firstAttribute.getValue()).isEqualTo("mps");
  }

  @Test
  public void shouldCheckIfMergeIsPreventedByExternalMergeTools_WithNoGitAttributes() {
    String source = "change_possibly_needing_merge_tool";
    String target = "removed_git_attributes";
    assertThat(attributeAnalyzer.hasExternalMergeToolConflicts(source, target)).isFalse();
  }

  @Test
  public void shouldCheckIfMergeIsPreventedByExternalMergeTools_WithEmptyGitAttributes() {
    String source = "change_possibly_needing_merge_tool";
    String target = "empty_git_attributes";
    assertThat(attributeAnalyzer.hasExternalMergeToolConflicts(source, target)).isFalse();
  }

  @Test
  public void shouldCheckIfMergeIsPreventedByExternalMergeTools_PatternFoundButNoMergeToolConfigured() {
    String source = "change_possibly_needing_merge_tool";
    String target = "removed_merge_tool_from_attributes";
    assertThat(attributeAnalyzer.hasExternalMergeToolConflicts(source, target)).isFalse();
  }

  @Test
  public void shouldCheckIfMergeIsPreventedByExternalMergeTools_WithoutMergeToolRelevantChange() {
    String source = "change_not_needing_merge_tool";
    String target = "conflicting_change_not_needing_merge_tool";
    assertThat(attributeAnalyzer.hasExternalMergeToolConflicts(source, target)).isFalse();
  }

  @Test
  public void shouldCheckIfMergeIsPreventedByExternalMergeTools_WithMergeToolRelevantChangeButFastForwardable() {
    String source = "change_possibly_needing_merge_tool";
    String target = "main";
    assertThat(attributeAnalyzer.hasExternalMergeToolConflicts(source, target)).isFalse();
  }

  @Test
  public void shouldCheckIfMergeIsPreventedByExternalMergeTools_WithMergeToolRelevantChangeAndPossibleConflict() {
    String source = "change_possibly_needing_merge_tool";
    String target = "conflicting_change_possibly_needing_merge_tool";
    assertThat(attributeAnalyzer.hasExternalMergeToolConflicts(source, target)).isTrue();
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-attributes-spi-test.zip";
  }
}
