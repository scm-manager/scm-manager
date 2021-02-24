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

import com.google.common.collect.Iterables;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;
import sonia.scm.repository.api.ImportFailedException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class GitLazyChangesetResolverTest extends AbstractGitCommandTestBase {

  @Test
  public void shouldResolveChangesets() throws IOException {
    GitLazyChangesetResolver changesetResolver = new GitLazyChangesetResolver(repository, Git.wrap(createContext().open()));
    Iterable<RevCommit> commits = changesetResolver.call();

    RevCommit firstCommit = commits.iterator().next();
    assertThat(firstCommit.getId().toString()).isEqualTo("commit a8495c0335a13e6e432df90b3727fa91943189a7 1602078219 -----sp");
    assertThat(firstCommit.getCommitTime()).isEqualTo(1602078219);
    assertThat(firstCommit.getFullMessage()).isEqualTo("add deeper paths\n");
  }

  @Test
  public void shouldResolveAllChangesets() throws IOException, GitAPIException {
    Git git = Git.wrap(createContext().open());
    GitLazyChangesetResolver changesetResolver = new GitLazyChangesetResolver(repository, git);
    Iterable<RevCommit> allCommits = changesetResolver.call();
    int allCommitsCounter = Iterables.size(allCommits);
    int singleBranchCommitsCounter = Iterables.size(git.log().call());

    assertThat(allCommitsCounter).isGreaterThan(singleBranchCommitsCounter);
  }

  @Test(expected = ImportFailedException.class)
  public void shouldThrowImportFailedException() {
    Git git = mock(Git.class);
    doThrow(ImportFailedException.class).when(git).log();
    new GitLazyChangesetResolver(repository, git).call();
  }
}

