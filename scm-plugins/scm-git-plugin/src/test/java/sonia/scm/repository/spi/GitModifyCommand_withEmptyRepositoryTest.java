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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.Person;
import sonia.scm.repository.util.NoneCachingWorkingCopyPool;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
public class GitModifyCommand_withEmptyRepositoryTest extends AbstractGitCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();
  @Rule
  public ShiroRule shiro = new ShiroRule();

  private final LfsBlobStoreFactory lfsBlobStoreFactory = mock(LfsBlobStoreFactory.class);

  @Test
  public void shouldCreateNewFileInEmptyRepository() throws IOException, GitAPIException {
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("new_file")).isTrue();

    assertInTree(assertions);
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-empty-repo.zip";
  }

  private void assertInTree(TreeAssertions assertions) throws IOException, GitAPIException {
    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      try (RevWalk walk = new RevWalk(git.getRepository())) {
        RevCommit commit = walk.parseCommit(lastCommit);
        ObjectId treeId = commit.getTree().getId();
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
          assertions.checkAssertions(new CanonicalTreeParser(null, reader, treeId));
        }
      }
    }
  }

  private RevCommit getLastCommit(Git git) throws GitAPIException {
    return git.log().setMaxCount(1).call().iterator().next();
  }

  private GitModifyCommand createCommand() {
    return new GitModifyCommand(createContext(), new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider())), lfsBlobStoreFactory);
  }

  @FunctionalInterface
  private interface TreeAssertions {
    void checkAssertions(CanonicalTreeParser treeParser) throws CorruptObjectException;
  }
}
