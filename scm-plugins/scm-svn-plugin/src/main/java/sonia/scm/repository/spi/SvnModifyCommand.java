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

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.ContextEntry;
import sonia.scm.NoChangesMadeException;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnWorkingCopyFactory;
import sonia.scm.repository.work.WorkingCopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.repository.spi.IntegrateChangesFromWorkdirException.withPattern;

public class SvnModifyCommand implements ModifyCommand {

  public static final Pattern SVN_ERROR_PATTERN = Pattern.compile(".*E" + SVNErrorCode.CANCELLED.getCode() + ": (.*)");

  private final SvnContext context;
  private final SvnWorkingCopyFactory workingCopyFactory;
  private final Repository repository;

  private final SvnFileLockCommand lockCommand;

  SvnModifyCommand(SvnContext context, SvnWorkingCopyFactory workingCopyFactory) {
    this.context = context;
    this.repository = context.getRepository();
    this.workingCopyFactory = workingCopyFactory;
    this.lockCommand = new SvnFileLockCommand(context);
  }

  @Override
  public String execute(ModifyCommandRequest request) {
    SVNClientManager clientManager = SVNClientManager.newInstance();
    try (WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null)) {
      File workingDirectory = workingCopy.getDirectory();
      if (!StringUtils.isEmpty(request.getExpectedRevision())
        && !request.getExpectedRevision().equals(getCurrentRevision(clientManager, workingCopy))) {
        throw new ConcurrentModificationException(entity(repository).build());
      }
      if (request.isDefaultPath()) {
        workingDirectory = Paths.get(workingDirectory.toString() + "/trunk").toFile();
      }
      modifyWorkingDirectory(request, clientManager, workingDirectory);
      return commitChanges(clientManager, workingDirectory, request.getCommitMessage());
    }
  }

  private String getCurrentRevision(SVNClientManager clientManager, WorkingCopy<File, File> workingCopy) {
    try {
      return Long.toString(clientManager.getStatusClient().doStatus(workingCopy.getWorkingRepository(), false).getRevision().getNumber());
    } catch (SVNException e) {
      throw new InternalRepositoryException(entity(repository), "Could not read status of working repository", e);
    }
  }

  private String getCurrentUserName() {
    if (SecurityUtils.getSubject() != null && SecurityUtils.getSubject().getPrincipal() != null) {
      return SecurityUtils.getSubject().getPrincipal().toString();
    } else {
      return "SCM-Manager";
    }
  }

  private String commitChanges(SVNClientManager clientManager, File workingDirectory, String commitMessage) {
    try {
      clientManager.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager(getCurrentUserName(), new char[0]));
      SVNCommitInfo svnCommitInfo = clientManager.getCommitClient().doCommit(
        new File[]{workingDirectory},
        false,
        commitMessage,
        null,
        null,
        false,
        true,
        SVNDepth.INFINITY
      );
      if (svnCommitInfo.toString().equals("EMPTY COMMIT")) {
        throw new NoChangesMadeException(repository);
      }
      return "head";
    } catch (SVNException e) {
      throw withPattern(SVN_ERROR_PATTERN).forMessage(repository, e.getErrorMessage().getRootErrorMessage().getFullMessage());
    }
  }

  private void modifyWorkingDirectory(ModifyCommandRequest request, SVNClientManager clientManager, File workingDirectory) {
    for (ModifyCommandRequest.PartialRequest partialRequest : request.getRequests()) {
      try {
        SVNWCClient wcClient = clientManager.getWCClient();
        partialRequest.execute(new ModifyWorker(wcClient, workingDirectory));
      } catch (IOException e) {
        throw new InternalRepositoryException(repository, "could not read files from repository");
      }
    }
  }

  private class ModifyWorker implements ModifyWorkerHelper {
    private final SVNWCClient wcClient;
    private final File workingDirectory;

    private ModifyWorker(SVNWCClient wcClient, File workingDirectory) {
      this.wcClient = wcClient;
      this.workingDirectory = workingDirectory;
    }

    @Override
    public void doScmDelete(String toBeDeleted) {
      unlock(toBeDeleted);
      try {
        wcClient.doDelete(new File(workingDirectory, toBeDeleted), true, true, false);
      } catch (SVNException e) {
        if (e.getErrorMessage().getErrorCode().getCode() == 125001) {
          throw new ModificationFailedException(ContextEntry.ContextBuilder.entity("File", toBeDeleted).in(repository).build(), "Could not remove file from repository");
        }
        throw new InternalRepositoryException(repository, "could not delete file from repository");
      }
    }

    @Override
    public void addFileToScm(String name, Path file) {
      unlock(name);
      try {
        wcClient.doAdd(
          file.toFile(),
          true,
          false,
          true,
          SVNDepth.INFINITY,
          false,
          true
        );
      } catch (SVNException e) {
        if (e.getErrorMessage().getErrorCode().getCode() == 155010) {
          throw new ModificationFailedException(ContextEntry.ContextBuilder.entity("File", name).in(repository).build(), "Could not add file to repository");
        }
        throw new InternalRepositoryException(repository, "could not add file to repository");
      }
    }

    private void unlock(String toBeDeleted) {
      lockCommand.unlock(
        createUnlockRequest(toBeDeleted),
        SvnFileLockCommand.SvnFileLock::isCreatedByScmManager
      );
    }

    private UnlockCommandRequest createUnlockRequest(String toBeDeleted) {
      UnlockCommandRequest request = new UnlockCommandRequest();
      request.setFile(toBeDeleted);
      return request;
    }

    @Override
    public File getWorkDir() {
      return workingDirectory;
    }

    @Override
    public Repository getRepository() {
      return repository;
    }

    @Override
    public String getBranch() {
      return null;
    }
  }
}
