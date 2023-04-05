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

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CannotDeleteCurrentBranchException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Branch;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.api.BranchRequest;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.eclipse.jgit.lib.ObjectId.zeroId;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

@Slf4j
public class GitBranchCommand extends AbstractGitCommand implements BranchCommand {

  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;
  private final GitChangesetConverterFactory converterFactory;

  @Inject
  GitBranchCommand(GitContext context, HookContextFactory hookContextFactory, ScmEventBus eventBus, GitChangesetConverterFactory converterFactory) {
    super(context);
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
    this.converterFactory = converterFactory;
  }

  @Override
  public Branch branch(BranchRequest request) {
    try (Git git = new Git(context.open())) {
      ObjectId newRef;
      if (request.getParentBranch() == null) {
        newRef = git.log().call().iterator().next();
      } else {
        newRef = getRef(git.getRepository(), request.getParentBranch());
      }
      RepositoryHookEvent hookEvent = createBranchHookEvent(createHookEvent(request.getNewBranch(), newRef));
      eventBus.post(new PreReceiveRepositoryHookEvent(hookEvent));
      Ref ref = git.branchCreate().setStartPoint(request.getParentBranch()).setName(request.getNewBranch()).call();
      eventBus.post(new PostReceiveRepositoryHookEvent(hookEvent));
      return Branch.normalBranch(request.getNewBranch(), GitUtil.getId(ref.getObjectId()));
    } catch (InvalidRefNameException e) {
      log.debug("got exception for invalid branch name {}", request.getNewBranch(), e);
      doThrow().violation("Invalid branch name", "name").when(true);
      return null;
    } catch (GitAPIException | IOException ex) {
      throw new InternalRepositoryException(repository, "could not create branch " + request.getNewBranch(), ex);
    }
  }

  @Override
  public void deleteOrClose(String branchName) {
    try (Git gitRepo = new Git(context.open())) {
      ObjectId oldRef = getRef(gitRepo.getRepository(), branchName);
      RepositoryHookEvent hookEvent = createBranchHookEvent(deleteHookEvent(branchName, oldRef));
      eventBus.post(new PreReceiveRepositoryHookEvent(hookEvent));
      gitRepo
        .branchDelete()
        .setBranchNames(branchName)
        .setForce(true)
        .call();
      eventBus.post(new PostReceiveRepositoryHookEvent(hookEvent));
    } catch (CannotDeleteCurrentBranchException e) {
      throw new CannotDeleteDefaultBranchException(context.getRepository(), branchName);
    } catch (GitAPIException | IOException ex) {
      throw new InternalRepositoryException(entity(context.getRepository()), String.format("Could not delete branch: %s", branchName));
    }
  }

  private RepositoryHookEvent createBranchHookEvent(BranchHookContextProvider hookEvent) {
    HookContext context = hookContextFactory.createContext(hookEvent, this.context.getRepository());
    return new RepositoryHookEvent(context, this.context.getRepository(), RepositoryHookType.PRE_RECEIVE);
  }

  private ObjectId getRef(Repository gitRepo, String branch) {
    try {
      return gitRepo.getRefDatabase().findRef("refs/heads/" + branch).getObjectId();
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "error reading ref for branch", e);
    }
  }

  private BranchHookContextProvider createHookEvent(String newBranch, ObjectId objectId) {
    return new BranchHookContextProvider(singletonList(newBranch), emptyList(), objectId);
  }

  private BranchHookContextProvider deleteHookEvent(String deletedBranch, ObjectId oldObjectId) {
    return new BranchHookContextProvider(emptyList(), singletonList(deletedBranch), oldObjectId);
  }

  private class BranchHookContextProvider extends HookContextProvider {
    private final List<String> newBranches;
    private final List<String> deletedBranches;
    private final ObjectId objectId;

    private BranchHookContextProvider(List<String> newBranches, List<String> deletedBranches, ObjectId objectId) {
      this.newBranches = newBranches;
      this.deletedBranches = deletedBranches;
      this.objectId = objectId;
    }

    @Override
    public Set<HookFeature> getSupportedFeatures() {
      return singleton(HookFeature.BRANCH_PROVIDER);
    }

    @Override
    public HookBranchProvider getBranchProvider() {
      return new HookBranchProvider() {
        @Override
        public List<String> getCreatedOrModified() {
          return newBranches;
        }

        @Override
        public List<String> getDeletedOrClosed() {
          return deletedBranches;
        }
      };
    }

    @Override
    public HookChangesetProvider getChangesetProvider() {
      Repository gitRepo;
      try {
        gitRepo = context.open();
      } catch (IOException e) {
        throw new InternalRepositoryException(repository, "failed to open repository for post receive hook after internal change", e);
      }

      Collection<ReceiveCommand> receiveCommands = new ArrayList<>();
      newBranches.stream()
        .map(branch -> new ReceiveCommand(zeroId(), objectId, "refs/heads/" + branch))
        .forEach(receiveCommands::add);
      deletedBranches.stream()
        .map(branch -> new ReceiveCommand(objectId, zeroId(), "refs/heads/" + branch))
        .forEach(receiveCommands::add);
      return x -> {
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
}
