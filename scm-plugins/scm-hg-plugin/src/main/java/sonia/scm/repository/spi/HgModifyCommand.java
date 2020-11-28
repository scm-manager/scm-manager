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

import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.CommitCommand;
import com.aragost.javahg.commands.ExecutionException;
import com.aragost.javahg.commands.PullCommand;
import com.aragost.javahg.commands.RemoveCommand;
import com.aragost.javahg.commands.StatusCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.NoChangesMadeException;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.work.WorkingCopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("java:S3252") // it is ok for javahg classes to access static method of subtype
public class HgModifyCommand implements ModifyCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgModifyCommand.class);
  static final Pattern HG_MESSAGE_PATTERN = Pattern.compile(".*\\[SCM\\](?: Error:)? (.*)");

  private final HgCommandContext context;
  private final HgWorkingCopyFactory workingCopyFactory;

  public HgModifyCommand(HgCommandContext context, HgWorkingCopyFactory workingCopyFactory) {
    this.context = context;
    this.workingCopyFactory = workingCopyFactory;
  }

  @Override
  public String execute(ModifyCommandRequest request) {
    try (WorkingCopy<com.aragost.javahg.Repository, com.aragost.javahg.Repository> workingCopy = workingCopyFactory.createWorkingCopy(context, request.getBranch())) {
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
                RemoveCommand.on(workingRepository).execute(toBeDeleted);
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
                workingRepository.workingCopy().add(file.getAbsolutePath());
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
        .user(String.format("%s <%s>", request.getAuthor().getName(), request.getAuthor().getMail()))
        .message(request.getCommitMessage()).execute();

      List<Changeset> execute = pullModifyChangesToCentralRepository(request, workingCopy);

      String node = execute.get(0).getNode();
      LOG.debug("successfully pulled changes from working copy, new node {}", node);
      return node;
    } catch (ExecutionException e) {
      throwInternalRepositoryException("could not execute command on repository", e);
      return null;
    }
  }

  private List<Changeset> pullModifyChangesToCentralRepository(ModifyCommandRequest request, WorkingCopy<com.aragost.javahg.Repository, com.aragost.javahg.Repository> workingCopy) {
    LOG.trace("pull changes from working copy");
    try {
      com.aragost.javahg.commands.PullCommand pullCommand = PullCommand.on(workingCopy.getCentralRepository());
      workingCopyFactory.configure(pullCommand);
      return pullCommand.execute(workingCopy.getDirectory().getAbsolutePath());
    } catch (ExecutionException e) {
      throw IntegrateChangesFromWorkdirException
        .withPattern(HG_MESSAGE_PATTERN)
        .forMessage(context.getScmRepository(), e.getMessage());
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getScmRepository(),
        String.format("Could not pull modify changes from working copy to central repository for branch %s", request.getBranch()),
        e);
    }
  }

  private String throwInternalRepositoryException(String message, Exception e) {
    throw new InternalRepositoryException(context.getScmRepository(), message, e);
  }
}
