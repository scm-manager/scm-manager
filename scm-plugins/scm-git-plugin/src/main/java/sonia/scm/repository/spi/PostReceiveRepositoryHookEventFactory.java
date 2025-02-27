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

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.FetchResult;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Tag;
import sonia.scm.repository.WrappedRepositoryHookEvent;

import java.util.List;
import java.util.stream.Collectors;

public class PostReceiveRepositoryHookEventFactory {

  private final ScmEventBus eventBus;
  private final GitRepositoryHookEventFactory eventFactory;
  private final GitContext context;

  @Inject
  PostReceiveRepositoryHookEventFactory(ScmEventBus eventBus, GitRepositoryHookEventFactory eventFactory, @Assisted GitContext context) {
    this.eventBus = eventBus;
    this.eventFactory = eventFactory;
    this.context = context;
  }

  void fireForFetch(Git git, FetchResult result) {
    PostReceiveRepositoryHookEvent event;
    List<String> branches = getBranchesFromFetchResult(result);
    List<Tag> tags = getTagsFromFetchResult(result);
    GitLazyChangesetResolver changesetResolver = new GitLazyChangesetResolver(context.getRepository(), git);
    event = new PostReceiveRepositoryHookEvent(WrappedRepositoryHookEvent.wrap(eventFactory.createPostReceiveEvent(context, branches, tags, changesetResolver)));
    eventBus.post(event);
  }

  private List<Tag> getTagsFromFetchResult(FetchResult result) {
    return result.getAdvertisedRefs().stream()
      .filter(r -> r.getName().startsWith("refs/tags/"))
      .map(r -> new Tag(r.getName().substring("refs/tags/".length()), r.getObjectId().getName()))
      .collect(Collectors.toList());
  }

  private List<String> getBranchesFromFetchResult(FetchResult result) {
    return result.getAdvertisedRefs().stream()
      .filter(r -> r.getName().startsWith("refs/heads/"))
      .map(r -> r.getLeaf().getName().substring("refs/heads/".length()))
      .collect(Collectors.toList());
  }

  public interface Factory {
    PostReceiveRepositoryHookEventFactory create(GitContext context);
  }

}
