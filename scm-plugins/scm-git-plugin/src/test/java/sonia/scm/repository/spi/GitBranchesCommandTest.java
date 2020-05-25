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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Branch;
import sonia.scm.repository.GitRepositoryConfig;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitBranchesCommandTest {

  @Mock
  GitContext context;
  @Mock
  Git git;
  @Mock
  ListBranchCommand listBranchCommand;
  @Mock
  GitRepositoryConfig gitRepositoryConfig;

  GitBranchesCommand branchesCommand;
  private Ref master;

  @BeforeEach
  void initContext() {
    when(context.getConfig()).thenReturn(gitRepositoryConfig);
  }

  @BeforeEach
  void initCommand() {
    master = createRef("master", "0000");
    branchesCommand = new GitBranchesCommand(context) {
      @Override
      Git createGit() {
        return git;
      }

      @Override
      Optional<Ref> getRepositoryHeadRef(Git git) {
        return of(master);
      }
    };
    when(git.branchList()).thenReturn(listBranchCommand);
  }

  @Test
  void shouldCreateEmptyListWithoutBranches() throws IOException, GitAPIException {
    when(listBranchCommand.call()).thenReturn(emptyList());

    List<Branch> branches = branchesCommand.getBranches();

    assertThat(branches).isEmpty();
  }

  @Test
  void shouldMapNormalBranch() throws IOException, GitAPIException {
    Ref branch = createRef("branch", "1337");
    when(listBranchCommand.call()).thenReturn(asList(branch));

    List<Branch> branches = branchesCommand.getBranches();

    assertThat(branches).containsExactly(Branch.normalBranch("branch", "1337"));
  }

  @Test
  void shouldMarkMasterBranchWithMasterFromConfig() throws IOException, GitAPIException {
    Ref branch = createRef("branch", "1337");
    when(listBranchCommand.call()).thenReturn(asList(branch));
    when(gitRepositoryConfig.getDefaultBranch()).thenReturn("branch");

    List<Branch> branches = branchesCommand.getBranches();

    assertThat(branches).containsExactlyInAnyOrder(Branch.defaultBranch("branch", "1337"));
  }

  @Test
  void shouldMarkMasterBranchWithMasterFromHead() throws IOException, GitAPIException {
    Ref branch = createRef("branch", "1337");
    when(listBranchCommand.call()).thenReturn(asList(branch, master));

    List<Branch> branches = branchesCommand.getBranches();

    assertThat(branches).containsExactlyInAnyOrder(
      Branch.normalBranch("branch", "1337"),
      Branch.defaultBranch("master", "0000")
    );
  }

  private Ref createRef(String branchName, String revision) {
    Ref ref = mock(Ref.class);
    lenient().when(ref.getName()).thenReturn("refs/heads/" + branchName);
    ObjectId objectId = mock(ObjectId.class);
    lenient().when(objectId.name()).thenReturn(revision);
    lenient().when(ref.getObjectId()).thenReturn(objectId);
    return ref;
  }
}
