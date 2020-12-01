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

import com.google.common.base.Strings;
import org.apache.shiro.SecurityUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import sonia.scm.NotFoundException;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookTagProvider;
import sonia.scm.repository.api.TagCreateRequest;
import sonia.scm.repository.api.TagDeleteRequest;
import sonia.scm.security.GPG;
import sonia.scm.user.User;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

public class GitTagCommand extends AbstractGitCommand implements TagCommand {
  private final GPG gpg;
  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;

  @Inject
  GitTagCommand(GitContext context, GPG gpg, HookContextFactory hookContextFactory, ScmEventBus eventBus) {
    super(context);
    this.gpg = gpg;
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
  }

  @Override
  public Tag create(TagCreateRequest request) {
    final String name = request.getName();
    final String revision = request.getRevision();

    if (Strings.isNullOrEmpty(revision)) {
      throw new IllegalArgumentException("Revision is required");
    }

    if (Strings.isNullOrEmpty(name)) {
      throw new IllegalArgumentException("Name is required");
    }

    try (Git git = new Git(context.open())) {

      RevObject revObject;
      Long tagTime;

      ObjectId taggedCommitObjectId = git.getRepository().resolve(revision);

      if (taggedCommitObjectId == null) {
        throw new NotFoundException("revision", revision);
      }

      try (RevWalk walk = new RevWalk(git.getRepository())) {
        revObject = walk.parseAny(taggedCommitObjectId);
        tagTime = GitUtil.getTagTime(walk, taggedCommitObjectId);
      }

      Tag tag = new Tag(name, revision, tagTime);

      RepositoryHookEvent hookEvent = createTagHookEvent(TagHookContextProvider.createHookEvent(tag));
      eventBus.post(new PreReceiveRepositoryHookEvent(hookEvent));

      User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
      PersonIdent taggerIdent = new PersonIdent(user.getDisplayName(), user.getMail());

//      Ref ref =
      git.tag()
        .setObjectId(revObject)
        .setTagger(taggerIdent)
        .setName(name)
        .call();

//      Uncomment lines once jgit added support for signing tags
//      try (RevWalk walk = new RevWalk(git.getRepository())) {
//        revObject = walk.parseTag(ref.getObjectId());
//        final Optional<Signature> tagSignature = GitUtil.getTagSignature(revObject, gpg, walk);
//        tagSignature.ifPresent(tag::addSignature);
//      }

      eventBus.post(new PostReceiveRepositoryHookEvent(hookEvent));

      return tag;
    } catch (IOException | GitAPIException ex) {
      throw new InternalRepositoryException(repository, "could not create tag " + name + " for revision " + revision, ex);
    }
  }

  @Override
  public void delete(TagDeleteRequest request) {
    String name = request.getName();

    if (Strings.isNullOrEmpty(name)) {
      throw new IllegalArgumentException("Name is required");
    }

    try (Git git = new Git(context.open())) {
      final Repository repository = git.getRepository();
      Optional<Ref> tagRef = findTagRef(git, name);
      Tag tag;

      // Deleting a non-existent tag is a valid action and simply succeeds without
      // anything happening.
      if (!tagRef.isPresent()) {
        return;
      }

      try (RevWalk walk = new RevWalk(repository)) {
        final RevCommit commit = GitUtil.getCommit(repository, walk, tagRef.get());
        Long tagTime = GitUtil.getTagTime(walk, tagRef.get().getObjectId());
        tag = new Tag(name, commit.name(), tagTime);
      }

      RepositoryHookEvent hookEvent = createTagHookEvent(TagHookContextProvider.deleteHookEvent(tag));
      eventBus.post(new PreReceiveRepositoryHookEvent(hookEvent));
      git.tagDelete().setTags(name).call();
      eventBus.post(new PostReceiveRepositoryHookEvent(hookEvent));
    } catch (GitAPIException | IOException e) {
      throw new InternalRepositoryException(repository, "could not delete tag " + name, e);
    }
  }

  private Optional<Ref> findTagRef(Git git, String name) throws GitAPIException {
    final String tagRef = "refs/tags/" + name;
    return git.tagList().call().stream().filter(it -> it.getName().equals(tagRef)).findAny();
  }

  private RepositoryHookEvent createTagHookEvent(TagHookContextProvider hookEvent) {
    HookContext context = hookContextFactory.createContext(hookEvent, this.context.getRepository());
    return new RepositoryHookEvent(context, this.context.getRepository(), RepositoryHookType.PRE_RECEIVE);
  }

private static class TagHookContextProvider extends HookContextProvider {
  private final List<Tag> newTags;
  private final List<Tag> deletedTags;

  private TagHookContextProvider(List<Tag> newTags, List<Tag> deletedTags) {
    this.newTags = newTags;
    this.deletedTags = deletedTags;
  }

  static TagHookContextProvider createHookEvent(Tag newTag) {
    return new TagHookContextProvider(singletonList(newTag), emptyList());
  }

  static TagHookContextProvider deleteHookEvent(Tag deletedTag) {
    return new TagHookContextProvider(emptyList(), singletonList(deletedTag));
  }

  @Override
  public Set<HookFeature> getSupportedFeatures() {
    return singleton(HookFeature.TAG_PROVIDER);
  }

  @Override
  public HookTagProvider getTagProvider() {
    return new HookTagProvider() {
      @Override
      public List<Tag> getCreatedTags() {
        return newTags;
      }

      @Override
      public List<Tag> getDeletedTags() {
        return deletedTags;
      }
    };
  }

  @Override
  public HookChangesetProvider getChangesetProvider() {
    return r -> new HookChangesetResponse(emptyList());
  }
}
}
