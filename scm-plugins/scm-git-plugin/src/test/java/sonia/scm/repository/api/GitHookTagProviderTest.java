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

package sonia.scm.repository.api;

import com.google.common.collect.Lists;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Tag;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GitHookTagProvider}.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHookTagProviderTest {

  private static final String ZERO = ObjectId.zeroId().getName();
  public static final String REVISION_1 = "b2002b64013e54b78eac251df0672bd5d6a83aa7";
  public static final String REVISION_2 = "e0f2be968b147ff7043684a7715d2fe852553db4";

  @Mock
  private ReceiveCommand command;

  @Mock
  private Repository repository;
  @Mock
  private RevWalk revWalk;

  private List<ReceiveCommand> commands;

  /**
   * Set up mocks for upcoming tests.
   */
  @Before
  public void setUpMocks() throws IOException {
    commands = Lists.newArrayList(command);
    when(revWalk.parseAny(any(ObjectId.class))).thenAnswer(invocationOnMock -> {
      ObjectId objectId = invocationOnMock.getArgument(0);
      RevObject revObject = mock(RevObject.class);
      String name = objectId.getName();
      when(revObject.name()).thenReturn(name);
      return revObject;
    });
  }

  /**
   * Tests {@link GitHookTagProvider#getCreatedTags()}.
   */
  @Test
  public void testGetCreatedTags() {
    try (MockedStatic<GitUtil> dummy = Mockito.mockStatic(GitUtil.class)) {
      String revision = REVISION_1;
      long timestamp = 1339416344000L;
      String tagName = "1.0.0";
      String ref = "refs/tags/" + tagName;

      dummy.when(() -> GitUtil.getTagTime(revWalk, ObjectId.fromString(revision))).thenReturn(timestamp);
      dummy.when(() -> GitUtil.getTagName(ref)).thenReturn(tagName);
      dummy.when(() -> GitUtil.getId(hasObjectId(revision))).thenReturn(revision);

      GitHookTagProvider provider = createProvider(ReceiveCommand.Type.CREATE, ref, revision, ZERO);

      assertTag(tagName, revision, timestamp, provider.getCreatedTags());
      assertThat(provider.getDeletedTags(), empty());
    }
  }

  /**
   * Tests {@link GitHookTagProvider#getDeletedTags()}.
   */
  @Test
  public void testGetDeletedTags() {
    try (MockedStatic<GitUtil> dummy = Mockito.mockStatic(GitUtil.class)) {
      String revision = REVISION_1;
      long timestamp = 1339416344000L;
      String tagName = "1.0.0";
      String ref = "refs/tags/" + tagName;

      dummy.when(() -> GitUtil.getTagTime(revWalk, ObjectId.fromString(revision))).thenReturn(timestamp);
      dummy.when(() -> GitUtil.getTagName(ref)).thenReturn(tagName);
      dummy.when(() -> GitUtil.getId(hasObjectId(revision))).thenReturn(revision);

      GitHookTagProvider provider = createProvider(ReceiveCommand.Type.DELETE, ref, ZERO, revision);

      assertThat(provider.getCreatedTags(), empty());
      assertTag("1.0.0", revision, 1339416344000L, provider.getDeletedTags());
    }
  }

  /**
   * Tests {@link GitHookTagProvider} with a branch ref instead of a tag.
   */
  @Test
  public void testWithBranch() {
    String revision = REVISION_1;
    GitHookTagProvider provider = createProvider(ReceiveCommand.Type.CREATE, "refs/heads/1.0.0", revision, revision);

    assertThat(provider.getCreatedTags(), empty());
    assertThat(provider.getDeletedTags(), empty());
  }

  /**
   * Tests {@link GitHookTagProvider} with update command pre receive.
   */
  @Test
  public void testUpdateTagsPreReceive() {
    try (MockedStatic<GitUtil> dummy = Mockito.mockStatic(GitUtil.class)) {
      String oldRevision = REVISION_2;
      String newRevision = REVISION_1;

      long timestamp = 1339416344000L;
      String tagName = "1.0.0";
      String ref = "refs/tags/" + tagName;

      dummy.when(() -> GitUtil.getTagTime(revWalk, ObjectId.fromString(oldRevision))).thenReturn(timestamp);
      dummy.when(() -> GitUtil.getTagTime(revWalk, ObjectId.fromString(newRevision))).thenReturn(null);
      dummy.when(() -> GitUtil.getTagName(ref)).thenReturn(tagName);
      dummy.when(() -> GitUtil.getId(hasObjectId(oldRevision))).thenReturn(oldRevision);
      dummy.when(() -> GitUtil.getId(hasObjectId(newRevision))).thenReturn(newRevision);

      GitHookTagProvider provider = createProvider(ReceiveCommand.Type.UPDATE, ref, newRevision, oldRevision);

      assertTag(tagName, newRevision, null, provider.getCreatedTags());
      assertTag(tagName, oldRevision, timestamp, provider.getDeletedTags());
    }
  }

  /**
   * Tests {@link GitHookTagProvider} with update command post receive.
   */
  @Test
  public void testUpdateTagsPostReceive() {
    try (MockedStatic<GitUtil> dummy = Mockito.mockStatic(GitUtil.class)) {
      String oldRevision = REVISION_2;
      String newRevision = REVISION_1;

      long timestamp = 1339416344000L;
      String tagName = "1.0.0";
      String ref = "refs/tags/" + tagName;

      dummy.when(() -> GitUtil.getTagTime(revWalk, ObjectId.fromString(newRevision))).thenReturn(timestamp);
      dummy.when(() -> GitUtil.getTagTime(revWalk, ObjectId.fromString(oldRevision))).thenReturn(null);
      dummy.when(() -> GitUtil.getTagName(ref)).thenReturn(tagName);
      dummy.when(() -> GitUtil.getId(hasObjectId(oldRevision))).thenReturn(oldRevision);
      dummy.when(() -> GitUtil.getId(hasObjectId(newRevision))).thenReturn(newRevision);

      GitHookTagProvider provider = createProvider(ReceiveCommand.Type.UPDATE, ref, newRevision, oldRevision);

      assertTag(tagName, newRevision, timestamp, provider.getCreatedTags());
      assertTag(tagName, oldRevision, null, provider.getDeletedTags());
    }
  }

  @Test
  public void shouldUnpeelAnnotatedTag() throws IOException {
    try (MockedStatic<GitUtil> dummy = Mockito.mockStatic(GitUtil.class)) {
      String revisionOfTag = REVISION_1;
      String revisionOfCommit = REVISION_2;
      long timestampOfTag = 6666666000L;
      long timestampOfCommit = 1339416344000L;
      String tagName = "1.0.0";
      String ref = "refs/tags/" + tagName;

      RevTag mockedTag = mock(RevTag.class);
      when(revWalk.parseAny(ObjectId.fromString(REVISION_1))).thenReturn(mockedTag);
      RevObject commitForTag = mock(RevObject.class);
      when(commitForTag.getName()).thenReturn(revisionOfCommit);
      when(mockedTag.getObject()).thenReturn(commitForTag);
      dummy.when(() -> GitUtil.getTagTime(revWalk, ObjectId.fromString(REVISION_1))).thenReturn(timestampOfTag);
      dummy.when(() -> GitUtil.getTagTime(revWalk, commitForTag)).thenReturn(timestampOfCommit);

      dummy.when(() -> GitUtil.getTagTime(revWalk, ObjectId.fromString(revisionOfTag))).thenReturn(timestampOfCommit);
      dummy.when(() -> GitUtil.getTagName(ref)).thenReturn(tagName);
      dummy.when(() -> GitUtil.getId(hasObjectId(revisionOfCommit))).thenReturn(revisionOfCommit);

      GitHookTagProvider provider = createProvider(ReceiveCommand.Type.CREATE, ref, revisionOfTag, ZERO);

      assertTag(tagName, revisionOfCommit, timestampOfCommit, provider.getCreatedTags());
      assertThat(provider.getDeletedTags(), empty());
    }
  }

  public AnyObjectId hasObjectId(String oldRevision) {
    return argThat(anyObjectId -> oldRevision.equals(anyObjectId.name()));
  }

  private void assertTag(String name, String revision, Long date, List<Tag> tags) {
    assertNotNull(tags);
    assertFalse(tags.isEmpty());
    assertEquals(1, tags.size());
    Tag tag = tags.get(0);
    assertEquals(name, tag.getName());
    assertEquals(revision, tag.getRevision());
    assertEquals(date, tag.getDate().orElse(null));
  }

  private GitHookTagProvider createProvider(ReceiveCommand.Type type, String ref, String newId, String oldId) {
    when(command.getNewId()).thenReturn(ObjectId.fromString(newId));
    when(command.getOldId()).thenReturn(ObjectId.fromString(oldId));
    when(command.getType()).thenReturn(type);
    when(command.getRefName()).thenReturn(ref);
    return new GitHookTagProvider(commands, repository) {
      @Override
      RevWalk createRevWalk(Repository repository) {
        return revWalk;
      }
    };
  }

}
