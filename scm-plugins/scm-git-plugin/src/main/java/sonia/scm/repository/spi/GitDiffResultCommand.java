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

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import sonia.scm.NotUniqueRevisionException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffResult;

import java.io.IOException;

public class GitDiffResultCommand extends AbstractGitCommand implements DiffResultCommand {

  @Inject
  GitDiffResultCommand(@Assisted GitContext context) {
    super(context);
  }

  public DiffResult getDiffResult(DiffCommandRequest diffCommandRequest) throws IOException {
    org.eclipse.jgit.lib.Repository repository = open();
    return new GitDiffResult(this.repository, repository, Differ.diff(repository, diffCommandRequest), 0, null);
  }

  @Override
  public DiffResult getDiffResult(DiffResultCommandRequest request) throws IOException {
    org.eclipse.jgit.lib.Repository repository = open();
    int offset = request.getOffset() == null ? 0 : request.getOffset();
    try {
      return new GitDiffResult(this.repository, repository, Differ.diff(repository, request), offset, request.getLimit());
    } catch (AmbiguousObjectException ex) {
      throw new NotUniqueRevisionException(Repository.class, context.getRepository().getId());
    }
  }

  public interface Factory {
    DiffResultCommand create(GitContext context);
  }

}
