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
