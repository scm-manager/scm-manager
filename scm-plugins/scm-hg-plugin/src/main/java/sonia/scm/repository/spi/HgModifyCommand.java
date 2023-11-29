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
import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import org.javahg.Changeset;
import org.javahg.Repository;
import org.javahg.commands.AddCommand;
import org.javahg.commands.CommitCommand;
import org.javahg.commands.ExecutionException;
import org.javahg.commands.RemoveCommand;
import org.javahg.commands.StatusCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.NoChangesMadeException;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.work.WorkingCopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static sonia.scm.repository.spi.UserFormatter.getUserStringFor;

@SuppressWarnings("java:S3252") // it is ok for javahg classes to access static method of subtype
public class HgModifyCommand extends AbstractWorkingCopyCommand implements ModifyCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgModifyCommand.class);

  @Inject
  public HgModifyCommand(@Assisted HgCommandContext context, HgRepositoryHandler handler) {
    super(context, handler.getWorkingCopyFactory());
  }

  @VisibleForTesting
  HgModifyCommand(HgCommandContext context, HgWorkingCopyFactory workingCopyFactory) {
    super(context, workingCopyFactory);
  }

  @Override
  public String execute(ModifyCommandRequest request) {
    try (WorkingCopy<org.javahg.Repository, org.javahg.Repository> workingCopy = workingCopyFactory.createWorkingCopy(context, request.getBranch())) {
      Repository workingRepository = workingCopy.getWorkingRepository();
      request.getRequests().forEach(
        partialRequest -> {
          try {
            partialRequest.execute(new ModifyWorkerHelper() {

              @Override
              public void addFileToScm(String name, Path file) {
                try {
                  addFileToHg(file.toFile());
                } catch (ExecutionException e) {
                  throwInternalRepositoryException("could not add new file to index", e);
                }
              }

              @Override
              public void doScmDelete(String toBeDeleted) {
                List<String> execute = RemoveCommand.on(workingRepository).execute(toBeDeleted);
                if (execute.isEmpty()) {
                  throw new ModificationFailedException(ContextEntry.ContextBuilder.entity("File", toBeDeleted).in(repository).build(), "Could not delete file from repository");
                }
              }

              @Override
              public sonia.scm.repository.Repository getRepository() {
                return context.getScmRepository();
              }

              @Override
              public String getBranch() {
                return request.getBranch();
              }

              public File getWorkDir() {
                return workingRepository.getDirectory();
              }

              private void addFileToHg(File file) {
                List<String> execute = AddCommand.on(workingRepository).execute(file.getAbsolutePath());
                if (execute.isEmpty()) {
                  throw new ModificationFailedException(ContextEntry.ContextBuilder.entity("File", file.getName()).in(repository).build(), "Could not add file to repository");
                }
              }

              @Override
              public boolean isProtectedPath(Path path) {
                return path.startsWith(workingRepository.getDirectory().toPath().normalize().resolve(".hg"));
              }
            });
          } catch (IOException e) {
            throwInternalRepositoryException("could not execute command on repository", e);
          }
        }
      );

      if (StatusCommand.on(workingRepository).lines().isEmpty()) {
        throw new NoChangesMadeException(context.getScmRepository());
      }

      LOG.trace("commit changes in working copy");
      CommitCommand.on(workingRepository)
        .user(getUserStringFor(request.getAuthor()))
        .message(request.getCommitMessage()).execute();

      List<Changeset> execute = pullChangesIntoCentralRepository(workingCopy, request.getBranch());

      String node = execute.get(0).getNode();
      LOG.debug("successfully pulled changes from working copy, new node {}", node);
      return node;
    } catch (ExecutionException e) {
      throwInternalRepositoryException("could not execute command on repository", e);
      return null;
    }
  }

  private void throwInternalRepositoryException(String message, Exception e) {
    throw new InternalRepositoryException(context.getScmRepository(), message, e);
  }

  public interface Factory {
    HgModifyCommand create(HgCommandContext context);
  }

}
