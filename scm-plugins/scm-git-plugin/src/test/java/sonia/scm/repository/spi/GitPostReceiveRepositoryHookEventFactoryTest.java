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

import com.google.common.collect.ImmutableList;
import org.eclipse.jgit.api.Git;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.RepositoryHookEvent;
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

  private GitRepositoryHookEventFactory eventFactory;

  @Before
  public void init() {
    HookContextFactory hookContextFactory = mock(HookContextFactory.class);
    hookContext = mock(HookContext.class, RETURNS_DEEP_STUBS);
    when(hookContextFactory.createContext(any(), eq(repository))).thenReturn(hookContext);
    GitChangesetConverterFactory converterFactory = mock(GitChangesetConverterFactory.class);
    eventFactory = new GitRepositoryHookEventFactory(hookContextFactory, converterFactory);
  }

  @Test
  public void shouldCreateEvent() throws IOException {
    ImmutableList<String> branches = ImmutableList.of("master", "develop");
    ImmutableList<Tag> tags = ImmutableList.of(new Tag("1.0", "123"), new Tag("2.0", "456"));
    ImmutableList<Changeset> changesets = ImmutableList.of(new Changeset("1", 0L, null, "first"));
    when(hookContext.getChangesetProvider().getChangesetList()).thenReturn(changesets);
    when(hookContext.getBranchProvider().getCreatedOrModified()).thenReturn(branches);
    when(hookContext.getTagProvider().getCreatedTags()).thenReturn(tags);

    RepositoryHookEvent event = eventFactory.createEvent(
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
