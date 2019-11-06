/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.repository.spi;

import com.google.common.base.Strings;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffCommandBuilder;

import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitDiffCommand extends AbstractGitCommand implements DiffCommand {

  private final GitWorkdirFactory workdirFactory;

  GitDiffCommand(GitContext context, Repository repository, GitWorkdirFactory workdirFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
  }

  @Override
  public DiffCommandBuilder.OutputStreamConsumer getDiffResult(DiffCommandRequest request) throws IOException {
    WorkingCopyCloser closer = new WorkingCopyCloser();
    if (Strings.isNullOrEmpty(request.getMergeChangeset())) {
      return computeDiff(request, open(), closer);
    } else {
      return inCloneWithPostponedClose(git -> new GitCloneWorker<DiffCommandBuilder.OutputStreamConsumer>(git) {
        @Override
        DiffCommandBuilder.OutputStreamConsumer run() throws IOException {
          ObjectId sourceRevision = resolveRevision(request.getRevision());
          try {
            getClone().merge()
              .setFastForward(MergeCommand.FastForwardMode.NO_FF)
              .setCommit(false) // we want to set the author manually
              .include(request.getRevision(), sourceRevision)
              .call();
          } catch (GitAPIException e) {
            throw new InternalRepositoryException(context.getRepository(), "could not merge branch " + request.getRevision() + " into " + request.getMergeChangeset(), e);
          }

          DiffCommandRequest clone = request.clone();
          clone.setRevision(sourceRevision.name());
          return computeDiff(request, getClone().getRepository(), closer);
        }
      }, workdirFactory, request.getMergeChangeset(), closer);
    }
  }

  private DiffCommandBuilder.OutputStreamConsumer computeDiff(DiffCommandRequest request, org.eclipse.jgit.lib.Repository repository, WorkingCopyCloser closer) throws IOException {
    Differ.Diff diff = Differ.diff(repository, request);

    return output -> {
      try (DiffFormatter formatter = new DiffFormatter(output)) {
        formatter.setRepository(repository);

        for (DiffEntry e : diff.getEntries()) {
          if (!e.getOldId().equals(e.getNewId())) {
            formatter.format(e);
          }
        }

        formatter.flush();
      } finally {
        closer.close();
      }
    };
  }
}
