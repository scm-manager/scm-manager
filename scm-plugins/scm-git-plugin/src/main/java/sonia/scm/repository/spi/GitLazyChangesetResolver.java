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
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ImportFailedException;

import java.io.IOException;
import java.util.concurrent.Callable;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class GitLazyChangesetResolver implements Callable<Iterable<RevCommit>> {
  private final Repository repository;
  private final Git git;

  public GitLazyChangesetResolver(Repository repository, Git git) {
    this.repository = repository;
    this.git = git;
  }

  @Override
  public Iterable<RevCommit> call() {
    try {
      return git.log().all().call();
    } catch (IOException | GitAPIException e) {
      throw new ImportFailedException(
        entity(repository).build(),
        "Could not resolve changesets for imported repository",
        e
      );
    }
  }
}
