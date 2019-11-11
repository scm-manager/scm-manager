/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Branch;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.api.BranchRequest;
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

public class GitBranchCommand extends AbstractGitCommand implements BranchCommand {

  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;

  GitBranchCommand(GitContext context, Repository repository, HookContextFactory hookContextFactory, ScmEventBus eventBus) {
    super(context, repository);
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
  }

  @Override
  public Branch branch(BranchRequest request) {
    try (Git git = new Git(context.open())) {
      RepositoryHookEvent hookEvent = createHookEvent(request);
      eventBus.post(new PreReceiveRepositoryHookEvent(hookEvent));
      Ref ref = git.branchCreate().setStartPoint(request.getParentBranch()).setName(request.getNewBranch()).call();
      eventBus.post(new PostReceiveRepositoryHookEvent(hookEvent));
      return Branch.normalBranch(request.getNewBranch(), GitUtil.getId(ref.getObjectId()));
    } catch (GitAPIException | IOException ex) {
      throw new InternalRepositoryException(repository, "could not create branch " + request.getNewBranch(), ex);
    }
  }

  private RepositoryHookEvent createHookEvent(BranchRequest request) {
    HookContext context = hookContextFactory.createContext(new BranchHookContextProvider(request), this.context.getRepository());
    return new RepositoryHookEvent(context, this.context.getRepository(), RepositoryHookType.PRE_RECEIVE);
  }

  private static class BranchHookContextProvider extends HookContextProvider {
    private final BranchRequest request;

    public BranchHookContextProvider(BranchRequest request) {
      this.request = request;
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
          return singletonList(request.getNewBranch());
        }

        @Override
        public List<String> getDeletedOrClosed() {
          return emptyList();
        }
      };
    }

    @Override
    public HookChangesetProvider getChangesetProvider() {
      return request -> new HookChangesetResponse(emptyList());
    }
  }
}
