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
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;
import java.util.Collections;

class GitFastForwardIfPossible extends GitMergeStrategy {

  private GitMergeStrategy fallbackMerge;

  GitFastForwardIfPossible(Git clone, MergeCommandRequest request, GitContext context, Repository repository) {
    super(clone, request, context, repository);
    fallbackMerge = new GitMergeCommit(clone, request, context, repository);
  }

  @Override
  MergeCommandResult run() throws IOException {
    MergeResult fastForwardResult = mergeWithFastForwardOnlyMode();
    if (fastForwardResult.getMergeStatus().isSuccessful()) {
      push();
      return createSuccessResult(fastForwardResult.getNewHead().name());
    } else {
      return fallbackMerge.run();
    }
  }

  private MergeResult mergeWithFastForwardOnlyMode() throws IOException {
    MergeCommand mergeCommand = getClone().merge();
    mergeCommand.setFastForward(MergeCommand.FastForwardMode.FF_ONLY);
    return doMergeInClone(mergeCommand);
  }
}
