/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
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
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.repository.client.spi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnCommitCommand implements CommitCommand {

  private final SVNClientManager client;
  private final File workingCopy;
  private final List<File> addedFiles;
  private final List<File> removedFiles;

  SvnCommitCommand(SVNClientManager client, File workingCopy, List<File> addedFiles, List<File> removedFiles) {
    this.client = client;
    this.workingCopy = workingCopy;
    this.addedFiles = addedFiles;
    this.removedFiles = removedFiles;
  }
  
  @Override
  public Changeset commit(CommitRequest request) throws IOException {
    SVNWCClient wClient = client.getWCClient();
    
    List<File> filesToCommit = new ArrayList<>();
    
    // add files
    try {
      wClient.doAdd(addedFiles.toArray(new File[0]), true, false, false,
                    SVNDepth.INFINITY, false, false, false);
      
      filesToCommit.addAll(addedFiles);
      addedFiles.clear();
      
    } catch (SVNException ex) {
      throw new IOException("failed to add files", ex);
    }
    
    // remove files
    try {
      Iterator<File> removeIt = removedFiles.iterator();
      while (removeIt.hasNext()) {
        File file = removeIt.next();
        wClient.doDelete(file, false, true, false);
        removeIt.remove();
        filesToCommit.add(file);
      }
    } catch (SVNException ex) {
      throw new IOException("failed to remove files", ex);
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
      throw new IOException("failed to commit", ex);
    }
    
    // get log for commit
    try {
      SVNRevision revision = SVNRevision.create(info.getNewRevision());
      
      SvnLog log = client.getOperationFactory().createLog();
      log.addRange(SvnRevisionRange.create(revision, revision));
      log.setSingleTarget(workingCopyTarget);
      
      changeset = SvnUtil.createChangeset(log.run());
    } catch (SVNException ex) {
      throw new IOException("failed to create log entry for last commit", ex);
    }
    
    return changeset;
  }
  
}
