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

