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
