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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.eclipse.jgit.lib.GpgSigner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.TagDeleteRequest;
import sonia.scm.repository.api.TagCreateRequest;
import sonia.scm.security.GPG;
import sonia.scm.util.MockUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitTagCommandTest extends AbstractGitCommandTestBase {

  @Mock
  private GPG gpg;

  @Mock
  private HookContextFactory hookContextFactory;

  @Mock
  private ScmEventBus eventBus;

  private Subject subject;

  @Before
  public void setSigner() {
    GpgSigner.setDefault(new GitTestHelper.SimpleGpgSigner());
  }

  @Before
  public void bindThreadContext() {
    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
    subject = MockUtil.createUserSubject(SecurityUtils.getSecurityManager());
    ThreadContext.bind(subject);
  }

  @After
  public void unbindThreadContext() {
    ThreadContext.unbindSubject();
    ThreadContext.unbindSecurityManager();
  }

  @Test
  public void shouldCreateATag() throws IOException {
    createCommand().create(new TagCreateRequest("592d797cd36432e591416e8b2b98154f4f163411", "newtag"));
    Optional<Tag> optionalTag = findTag(createContext(), "newtag");
    assertThat(optionalTag).isNotEmpty();
    final Tag tag = optionalTag.get();
    assertThat(tag.getName()).isEqualTo("newtag");
    assertThat(tag.getRevision()).isEqualTo("592d797cd36432e591416e8b2b98154f4f163411");
  }

  @Test
  public void shouldPostCreateEvent() {
      ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
      doNothing().when(eventBus).post(captor.capture());
      when(hookContextFactory.createContext(any(), any())).thenAnswer(this::createMockedContext);

      createCommand().create(new TagCreateRequest("592d797cd36432e591416e8b2b98154f4f163411", "newtag"));

      List<Object> events = captor.getAllValues();
      assertThat(events.get(0)).isInstanceOf(PreReceiveRepositoryHookEvent.class);
      assertThat(events.get(1)).isInstanceOf(PostReceiveRepositoryHookEvent.class);

      PreReceiveRepositoryHookEvent event = (PreReceiveRepositoryHookEvent) events.get(0);
      assertThat(event.getContext().getTagProvider().getCreatedTags().get(0).getName()).isEqualTo("newtag");
      assertThat(event.getContext().getTagProvider().getDeletedTags()).isEmpty();
  }

  @Test
  public void shouldDeleteATag() throws IOException {
    final GitContext context = createContext();
    Optional<Tag> tag = findTag(context, "test-tag");
    assertThat(tag).isNotEmpty();

    createCommand().delete(new TagDeleteRequest("test-tag"));

    tag = findTag(context, "test-tag");
    assertThat(tag).isEmpty();
  }

  @Test
  public void shouldPostDeleteEvent() {
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    doNothing().when(eventBus).post(captor.capture());
    when(hookContextFactory.createContext(any(), any())).thenAnswer(this::createMockedContext);

    createCommand().delete(new TagDeleteRequest("test-tag"));

    List<Object> events = captor.getAllValues();
    assertThat(events.get(0)).isInstanceOf(PreReceiveRepositoryHookEvent.class);
    assertThat(events.get(1)).isInstanceOf(PostReceiveRepositoryHookEvent.class);

    PreReceiveRepositoryHookEvent event = (PreReceiveRepositoryHookEvent) events.get(0);
    assertThat(event.getContext().getTagProvider().getCreatedTags()).isEmpty();
    final Tag deletedTag = event.getContext().getTagProvider().getDeletedTags().get(0);
    assertThat(deletedTag.getName()).isEqualTo("test-tag");
    assertThat(deletedTag.getRevision()).isEqualTo("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");
  }

  private GitTagCommand createCommand() {
    return new GitTagCommand(createContext(), hookContextFactory, eventBus);
  }

  private List<Tag> readTags(GitContext context) throws IOException {
    return new GitTagsCommand(context, gpg).getTags();
  }

  private Optional<Tag> findTag(GitContext context, String name) throws IOException {
    List<Tag> tags = readTags(context);
    return tags.stream().filter(t -> name.equals(t.getName())).findFirst();
  }

  private HookContext createMockedContext(InvocationOnMock invocation) {
    HookContext mock = mock(HookContext.class);
    when(mock.getTagProvider()).thenReturn(((HookContextProvider) invocation.getArgument(0)).getTagProvider());
    return mock;
  }
}
