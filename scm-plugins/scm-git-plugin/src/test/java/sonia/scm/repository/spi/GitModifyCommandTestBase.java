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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.GpgSigner;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.BeforeClass;
import org.junit.Rule;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static sonia.scm.repository.spi.GitRepositoryConfigStoreProviderTestUtil.createGitRepositoryConfigStoreProvider;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
class GitModifyCommandTestBase extends AbstractGitCommandTestBase {

  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();
  @Rule
  public ShiroRule shiro = new ShiroRule();

  final LfsBlobStoreFactory lfsBlobStoreFactory = mock(LfsBlobStoreFactory.class);

  @BeforeClass
  public static void setSigner() {
    GpgSigner.setDefault(new GitTestHelper.SimpleGpgSigner());
  }

  RevCommit getLastCommit(Git git) throws GitAPIException, IOException {
    return git.log().setMaxCount(1).call().iterator().next();
  }

  GitModifyCommand createCommand() {
    return new GitModifyCommand(
      createContext(),
      new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider(null, repositoryLocationResolver)), new SimpleMeterRegistry()),
      lfsBlobStoreFactory,
      createGitRepositoryConfigStoreProvider());
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
