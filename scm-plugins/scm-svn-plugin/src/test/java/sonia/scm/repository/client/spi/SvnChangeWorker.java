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
    
package sonia.scm.repository.client.spi;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc2.SvnCommit;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnRevisionRange;
import org.tmatesoft.svn.core.wc2.SvnTarget;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.client.api.RepositoryClientException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class SvnChangeWorker {
  
  private final File workingCopy;
  private final List<File> addedFiles = new ArrayList<>();
  private final List<File> removedFiles = new ArrayList<>();

  public SvnChangeWorker(File workingCopy) {
    this.workingCopy = workingCopy;
  }

  public AddCommand addCommand() {
    return new SvnAddCommand();
  }

  public RemoveCommand removeCommand() {
    return new SvnRemoveCommand();
  }

  public CommitCommand commitCommand(SVNClientManager client) {
    return new SvnCommitCommand(client);
  }

  private class SvnAddCommand implements AddCommand {
    @Override
    public void add(String path) throws IOException {
      addedFiles.add(toFile(path));
    }
  }

  private class SvnRemoveCommand implements RemoveCommand {
    @Override
    public void remove(String path) throws IOException {
      removedFiles.add(toFile(path));
    }
  }

  private class SvnCommitCommand implements CommitCommand {
    private final SVNClientManager client;

    private SvnCommitCommand(SVNClientManager client) {
      this.client = client;
    }

    @Override
    public Changeset commit(CommitRequest request) throws IOException {
      SVNWCClient wClient = client.getWCClient();

      // add files
      if (!addedFiles.isEmpty()){
        try {
          wClient.doAdd(addedFiles.toArray(new File[0]), true, false, false,
            SVNDepth.INFINITY, false, false, false);
          addedFiles.clear();

        } catch (SVNException ex) {
          throw new RepositoryClientException("failed to add files", ex);
        }
      }

      // remove files
      try {
        Iterator<File> removeIt = removedFiles.iterator();
        while (removeIt.hasNext()) {
          File file = removeIt.next();
          wClient.doDelete(file, false, true, false);
          removeIt.remove();
        }
      } catch (SVNException ex) {
        throw new RepositoryClientException("failed to remove files", ex);
      }

      SvnTarget workingCopyTarget = SvnTarget.fromFile(workingCopy);
      Changeset changeset;
      SVNCommitInfo info;

      // commit files
      try {
        SvnCommit commit = client.getOperationFactory().createCommit();
        commit.setDepth(SVNDepth.INFINITY);
        commit.setCommitMessage(request.getMessage());
        commit.setSingleTarget(workingCopyTarget);

        info = commit.run();

        SVNErrorMessage msg = info.getErrorMessage();
        if (msg != null) {
          throw new IOException(msg.getFullMessage());
        }

      } catch (SVNException ex) {
        throw new RepositoryClientException("failed to commit", ex);
      }

      // get log for commit
      try {
        SVNRevision revision = SVNRevision.create(info.getNewRevision());

        SvnLog log = client.getOperationFactory().createLog();
        log.addRange(SvnRevisionRange.create(revision, revision));
        log.setSingleTarget(workingCopyTarget);

        changeset = SvnUtil.createChangeset(log.run());
      } catch (SVNException ex) {
        throw new RepositoryClientException("failed to create log entry for last commit", ex);
      }

      return changeset;
    }
  }


  protected File toFile(String path) throws FileNotFoundException {
    File file = new File(workingCopy, path);
    if (!file.exists()) {
      throw new FileNotFoundException("could not find file ".concat(path));
    }
    return file;
  }
}
