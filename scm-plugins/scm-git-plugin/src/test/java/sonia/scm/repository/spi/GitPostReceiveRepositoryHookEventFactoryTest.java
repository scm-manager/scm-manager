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

import com.google.common.collect.ImmutableList;
import org.eclipse.jgit.api.Git;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitPostReceiveRepositoryHookEventFactoryTest extends AbstractGitCommandTestBase {

  private HookContext hookContext;

  private GitPostReceiveRepositoryHookEventFactory eventFactory;

  @Before
  public void init() {
    HookContextFactory hookContextFactory = mock(HookContextFactory.class);
    hookContext = mock(HookContext.class, RETURNS_DEEP_STUBS);
    when(hookContextFactory.createContext(any(), eq(repository))).thenReturn(hookContext);
    GitChangesetConverterFactory converterFactory = mock(GitChangesetConverterFactory.class);
    eventFactory = new GitPostReceiveRepositoryHookEventFactory(hookContextFactory, converterFactory);
  }

  @Test
  public void shouldCreateEvent() throws IOException {
    ImmutableList<String> branches = ImmutableList.of("master", "develop");
    ImmutableList<Tag> tags = ImmutableList.of(new Tag("1.0", "123"), new Tag("2.0", "456"));
    ImmutableList<Changeset> changesets = ImmutableList.of(new Changeset("1", 0L, null, "first"));
    when(hookContext.getChangesetProvider().getChangesetList()).thenReturn(changesets);
    when(hookContext.getBranchProvider().getCreatedOrModified()).thenReturn(branches);
    when(hookContext.getTagProvider().getCreatedTags()).thenReturn(tags);

    PostReceiveRepositoryHookEvent event = eventFactory.createEvent(
      createContext(),
      branches,
      tags,
      new GitLazyChangesetResolver(repository, Git.wrap(createContext().open()))
    );

    assertThat(event.getContext().getBranchProvider().getCreatedOrModified()).isSameAs(branches);
    assertThat(event.getContext().getTagProvider().getCreatedTags()).isSameAs(tags);
    assertThat(event.getContext().getChangesetProvider().getChangesetList().get(0).getId()).isEqualTo("1");
  }
}
