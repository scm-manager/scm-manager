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

import com.google.common.base.Predicate;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CannotDeleteCurrentBranchException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Branch;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryPredicate;
import sonia.scm.repository.api.BranchRequest;
import sonia.scm.repository.api.BranchesCommandBuilder;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class GitBranchCommand extends AbstractGitCommand implements BranchCommand {

  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;
  private final Cache<?, ?> cache;

  GitBranchCommand(GitContext context, HookContextFactory hookContextFactory, ScmEventBus eventBus, CacheManager cacheManager) {
    super(context);
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
    this.cache = cacheManager.getCache(BranchesCommandBuilder.CACHE_NAME);
  }

  @Override
  public Branch branch(BranchRequest request) {
    try (Git git = new Git(context.open())) {
      RepositoryHookEvent hookEvent = createBranchHookEvent(BranchHookContextProvider.createHookEvent(request.getNewBranch()));
      eventBus.post(new PreReceiveRepositoryHookEvent(hookEvent));
      Ref ref = git.branchCreate().setStartPoint(request.getParentBranch()).setName(request.getNewBranch()).call();
      clearCache(hookEvent);
      eventBus.post(new PostReceiveRepositoryHookEvent(hookEvent));
      return Branch.normalBranch(request.getNewBranch(), GitUtil.getId(ref.getObjectId()));
    } catch (GitAPIException | IOException ex) {
      throw new InternalRepositoryException(repository, "could not create branch " + request.getNewBranch(), ex);
    }
  }

  @Override
  public void deleteOrClose(String branchName) {
    try (Git gitRepo = new Git(context.open())) {
      RepositoryHookEvent hookEvent = createBranchHookEvent(BranchHookContextProvider.deleteHookEvent(branchName));
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

  private void clearCache(RepositoryHookEvent event) {
    if (event != null) {
      cache.removeAll((Predicate) new RepositoryPredicate(event));
    } else {
      cache.clear();
    }
  }

  private RepositoryHookEvent createBranchHookEvent(BranchHookContextProvider hookEvent) {
    HookContext context = hookContextFactory.createContext(hookEvent, this.context.getRepository());
    return new RepositoryHookEvent(context, this.context.getRepository(), RepositoryHookType.PRE_RECEIVE);
  }

  private static class BranchHookContextProvider extends HookContextProvider {
    private final List<String> newBranches;
    private final List<String> deletedBranches;

    private BranchHookContextProvider(List<String> newBranches, List<String> deletedBranches) {
      this.newBranches = newBranches;
      this.deletedBranches = deletedBranches;
    }

    static BranchHookContextProvider createHookEvent(String newBranch) {
      return new BranchHookContextProvider(singletonList(newBranch), emptyList());
    }

    static BranchHookContextProvider deleteHookEvent(String deletedBranch) {
      return new BranchHookContextProvider(emptyList(), singletonList(deletedBranch));
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
      return r -> new HookChangesetResponse(emptyList());
    }
  }
}
