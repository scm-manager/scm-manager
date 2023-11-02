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

import com.google.inject.assistedinject.Assisted;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.FetchResult;
import sonia.scm.ContextEntry;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Tag;
import sonia.scm.repository.WrappedRepositoryHookEvent;
import sonia.scm.repository.api.ImportFailedException;

import javax.inject.Inject;
import java.io.IOException;
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
    try {
      List<String> branches = getBranchesFromFetchResult(result);
      List<Tag> tags = getTagsFromFetchResult(result);
      GitLazyChangesetResolver changesetResolver = new GitLazyChangesetResolver(context.getRepository(), git);
      event = new PostReceiveRepositoryHookEvent(WrappedRepositoryHookEvent.wrap(eventFactory.createEvent(context, branches, tags, changesetResolver)));
    } catch (IOException e) {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(context.getRepository()).build(),
        "Could not fire post receive repository hook event after fetch",
        e
      );
    }
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
