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
