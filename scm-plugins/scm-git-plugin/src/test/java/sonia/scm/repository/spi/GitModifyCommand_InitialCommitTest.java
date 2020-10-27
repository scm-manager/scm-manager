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
import org.eclipse.jgit.lib.GpgSigner;
import org.eclipse.jgit.lib.Ref;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.Person;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
public class GitModifyCommand_InitialCommitTest extends AbstractGitCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public ShiroRule shiro = new ShiroRule();
  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();

  @BeforeClass
  public static void setSigner() {
    GpgSigner.setDefault(new GitTestHelper.SimpleGpgSigner());
  }

  @Test
  public void shouldCreateCommitOnMasterByDkefault() throws IOException, GitAPIException {
    createContext().getGlobalConfig().setDefaultBranch("");

    executeModifyCommand();

    try (Git git = new Git(createContext().open())) {
      List<Ref> branches = git.branchList().call();
      assertThat(branches).extracting("name").containsExactly("refs/heads/master");
    }
  }

  @Test
  public void shouldCreateCommitWithConfiguredDefaultBranch() throws IOException, GitAPIException {
    createContext().getGlobalConfig().setDefaultBranch("main");

    executeModifyCommand();

    try (Git git = new Git(createContext().open())) {
      List<Ref> branches = git.branchList().call();
      assertThat(branches).extracting("name").containsExactly("refs/heads/main");
    }
  }

  private void executeModifyCommand() throws IOException {
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("initial commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  private GitModifyCommand createCommand() {
    return new GitModifyCommand(createContext(), new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider())), mock(LfsBlobStoreFactory.class));
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-empty-repo-test.zip";
  }
}
