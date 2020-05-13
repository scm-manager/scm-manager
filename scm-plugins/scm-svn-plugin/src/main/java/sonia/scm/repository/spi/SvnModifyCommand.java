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

import org.apache.shiro.SecurityUtils;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnWorkDirFactory;
import sonia.scm.repository.util.WorkingCopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class SvnModifyCommand implements ModifyCommand {

  private SvnContext context;
  private SvnWorkDirFactory workDirFactory;
  private Repository repository;

  SvnModifyCommand(SvnContext context, SvnWorkDirFactory workDirFactory) {
    this.context = context;
    this.repository = context.getRepository();
    this.workDirFactory = workDirFactory;
  }

  @Override
  public String execute(ModifyCommandRequest request) {
    SVNClientManager clientManager = SVNClientManager.newInstance();
    try (WorkingCopy<File, File> workingCopy = workDirFactory.createWorkingCopy(context, null)) {
      File workingDirectory = workingCopy.getDirectory();
      modifyWorkingDirectory(request, clientManager, workingDirectory);
      return commitChanges(clientManager, workingDirectory, request.getCommitMessage());
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
      return String.valueOf(svnCommitInfo.getNewRevision());
    } catch (SVNException e) {
      throw new InternalRepositoryException(repository, "could not commit changes on repository");
    }
  }

  private String getCurrentUserName() {
    if (SecurityUtils.getSubject() != null && SecurityUtils.getSubject().getPrincipal() != null) {
      return SecurityUtils.getSubject().getPrincipal().toString();
    } else {
      return "SCM-Manager";
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
      try {
        wcClient.doDelete(new File(workingDirectory, toBeDeleted), true, true, false);
      } catch (SVNException e) {
        throw new InternalRepositoryException(repository, "could not delete file from repository");
      }
    }

    @Override
    public void addFileToScm(String name, Path file) {
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
        throw new InternalRepositoryException(repository, "could not add file to repository");
      }
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
