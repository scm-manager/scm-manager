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

import com.google.common.base.Strings;
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidTagNameException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookChangesetProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookTagProvider;
import sonia.scm.repository.api.TagCreateRequest;
import sonia.scm.repository.api.TagDeleteRequest;
import sonia.scm.user.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.eclipse.jgit.lib.ObjectId.fromString;
import static org.eclipse.jgit.lib.ObjectId.zeroId;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

@Slf4j
public class GitTagCommand extends AbstractGitCommand implements TagCommand {
  public static final String REFS_TAGS_PREFIX = "refs/tags/";
  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;
  private final GitChangesetConverterFactory converterFactory;

  @Inject
  GitTagCommand(@Assisted GitContext context, HookContextFactory hookContextFactory, ScmEventBus eventBus, GitChangesetConverterFactory converterFactory) {
    super(context);
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
    this.converterFactory = converterFactory;
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
        throw notFound(entity("revision", revision).in(repository));
      }

      try (RevWalk walk = new RevWalk(git.getRepository())) {
        revObject = walk.parseAny(taggedCommitObjectId);
        tagTime = GitUtil.getTagTime(walk, taggedCommitObjectId);
      }

      Tag tag = new Tag(name, revision, tagTime);

      RepositoryHookEvent hookEvent = createTagHookEvent(createHookEvent(tag), RepositoryHookType.PRE_RECEIVE);
      eventBus.post(new PreReceiveRepositoryHookEvent(hookEvent));

      User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
      PersonIdent taggerIdent = new PersonIdent(user.getDisplayName(), user.getMail());

      git.tag()
        .setObjectId(revObject)
        .setTagger(taggerIdent)
        .setName(name)
        .call();

      eventBus.post(new PostReceiveRepositoryHookEvent(hookEvent));

      return tag;
    } catch (InvalidTagNameException e) {
      log.debug("got exception for invalid tag name {}", request.getName(), e);
      doThrow().violation("Invalid tag name", "name").when(true);
      return null;
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
      if (tagRef.isEmpty()) {
        return;
      }

      try (RevWalk walk = new RevWalk(repository)) {
        final RevCommit commit = GitUtil.getCommit(repository, walk, tagRef.get());
        Long tagTime = GitUtil.getTagTime(walk, tagRef.get().getObjectId());
        tag = new Tag(name, commit.name(), tagTime);
      }

      eventBus.post(new PreReceiveRepositoryHookEvent(
        createTagHookEvent(deleteHookEvent(tag), RepositoryHookType.PRE_RECEIVE)
      ));
      git.tagDelete().setTags(name).call();
      eventBus.post(new PostReceiveRepositoryHookEvent(
        createTagHookEvent(deleteHookEvent(tag), RepositoryHookType.POST_RECEIVE)
      ));
    } catch (GitAPIException | IOException e) {
      throw new InternalRepositoryException(repository, "could not delete tag " + name, e);
    }
  }

  private Optional<Ref> findTagRef(Git git, String name) throws GitAPIException {
    final String tagRef = REFS_TAGS_PREFIX + name;
    return git.tagList().call().stream().filter(it -> it.getName().equals(tagRef)).findAny();
  }

  private RepositoryHookEvent createTagHookEvent(TagHookContextProvider hookEvent, RepositoryHookType type) {
    HookContext context = hookContextFactory.createContext(hookEvent, this.context.getRepository());
    return new RepositoryHookEvent(context, this.context.getRepository(), type);
  }

  private TagHookContextProvider createHookEvent(Tag newTag) {
    return new TagHookContextProvider(singletonList(newTag), emptyList());
  }

  private TagHookContextProvider deleteHookEvent(Tag deletedTag) {
    return new TagHookContextProvider(emptyList(), singletonList(deletedTag));
  }

  private class TagHookContextProvider extends HookContextProvider {
    private final List<Tag> newTags;
    private final List<Tag> deletedTags;

    private TagHookContextProvider(List<Tag> newTags, List<Tag> deletedTags) {
      this.newTags = newTags;
      this.deletedTags = deletedTags;
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
      Collection<ReceiveCommand> receiveCommands = new ArrayList<>();
      newTags.stream()
        .map(tag -> new ReceiveCommand(zeroId(), fromString(tag.getRevision()), REFS_TAGS_PREFIX + tag.getName()))
        .forEach(receiveCommands::add);
      deletedTags.stream()
        .map(tag -> new ReceiveCommand(fromString(tag.getRevision()), zeroId(), REFS_TAGS_PREFIX + tag.getName()))
        .forEach(receiveCommands::add);
      return x -> {
        Repository gitRepo = context.open();
        GitHookChangesetCollector collector =
          GitHookChangesetCollector.collectChangesets(
            converterFactory,
            receiveCommands,
            gitRepo,
            new RevWalk(gitRepo),
            commit -> false // we cannot create new commits with this tag command
          );
        return new HookChangesetResponse(collector.getAddedChangesets(), collector.getRemovedChangesets());
      };
    }
  }

  public interface Factory {
    TagCommand create(GitContext context);
  }

}
