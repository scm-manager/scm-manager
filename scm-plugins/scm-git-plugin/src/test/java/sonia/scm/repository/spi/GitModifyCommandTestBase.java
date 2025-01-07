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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.GpgConfig;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Signers;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.BeforeClass;
import org.junit.Rule;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.RepositoryHookType.POST_RECEIVE;
import static sonia.scm.repository.RepositoryHookType.PRE_RECEIVE;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
class GitModifyCommandTestBase extends AbstractGitCommandTestBase {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  final LfsBlobStoreFactory lfsBlobStoreFactory = mock(LfsBlobStoreFactory.class);
  final RepositoryManager repositoryManager = mock(RepositoryManager.class);

  @BeforeClass
  public static void setSigner() {
    Signers.set(GpgConfig.GpgFormat.OPENPGP, new GitTestHelper.SimpleGpgSigner());
  }

  RevCommit getLastCommit(Git git) throws GitAPIException, IOException {
    return git.log().setMaxCount(1).call().iterator().next();
  }

  GitModifyCommand createCommand() {
    GitRepositoryHookEventFactory eventFactory = mock(GitRepositoryHookEventFactory.class);
    RepositoryHookEvent preReceiveEvent = mockEvent(PRE_RECEIVE);
    when(eventFactory.createPreReceiveEvent(any(), any(), any(), any())).thenReturn(preReceiveEvent);
    RepositoryHookEvent postReceiveEvent = mockEvent(POST_RECEIVE);
    when(eventFactory.createPostReceiveEvent(any(), any(), any(), any())).thenReturn(postReceiveEvent);
    return new GitModifyCommand(
      createContext(),
      lfsBlobStoreFactory,
      repositoryManager,
      eventFactory
    );
  }

  private static RepositoryHookEvent mockEvent(RepositoryHookType type) {
    RepositoryHookEvent mock = mock(RepositoryHookEvent.class);
    when(mock.getType()).thenReturn(type);
    return mock;
  }

  void assertInTree(TreeAssertions assertions) throws IOException, GitAPIException {
    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      try (RevWalk walk = new RevWalk(git.getRepository())) {
        RevCommit commit = walk.parseCommit(lastCommit);
        ObjectId treeId = commit.getTree().getId();
        assertions.checkAssertions(path -> TreeWalk.forPath(git.getRepository(), path, treeId) != null);
      }
    }
  }

  @FunctionalInterface
  interface TreeAssertions {
    void checkAssertions(FileFinder fileFinder) throws IOException;
  }

  @FunctionalInterface
  interface FileFinder {
    boolean findFile(String path) throws IOException;
  }
}
