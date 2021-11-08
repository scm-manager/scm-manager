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
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.FileLockedException;
import sonia.scm.repository.api.LockCommandResult;
import sonia.scm.repository.api.UnlockCommandResult;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.tmatesoft.svn.core.auth.BasicAuthenticationManager.newInstance;
import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class SvnFileLockCommand extends AbstractSvnCommand implements FileLockCommand {

  protected SvnFileLockCommand(SvnContext context) {
    super(context);
  }

  @Override
  public LockCommandResult lock(LockCommandRequest request) {
    String fileToLock = request.getFile();
    try {
      doLock(fileToLock);
      return new LockCommandResult(true);
    } catch (SVNException e) {
      throw new InternalRepositoryException(entity("File", fileToLock).in(repository), "failed to lock file", e);
    }
  }

  private void doLock(String fileToLock) throws SVNException {
    SVNRepository svnRepository = open();
    String currentUser = getCurrentUser();
    ISVNAuthenticationManager authenticationManager = newInstance(currentUser, null);
    svnRepository.setAuthenticationManager(authenticationManager);
    getFileLock(fileToLock, svnRepository)
      .ifPresent(lock -> {
        throw new FileLockedException(repository.getNamespaceAndName(), lock);
      });
    svnRepository.lock(singletonMap(fileToLock, null), "locked by SCM-Manager for " + currentUser, false, null);
  }

  @Override
  public UnlockCommandResult unlock(UnlockCommandRequest request) {
    String fileToUnlock = request.getFile();
    try {
      doUnlock(request, fileToUnlock);
      return new UnlockCommandResult(true);
    } catch (SVNException e) {
      throw new InternalRepositoryException(entity("File", fileToUnlock).in(repository), "failed to lock file", e);
    }
  }

  private void doUnlock(UnlockCommandRequest request, String fileToUnlock) throws SVNException {
    SVNRepository svnRepository = open();
    Optional<FileLock> fileLock = getFileLock(fileToUnlock, svnRepository);
    if (fileLock.isPresent()) {
      if (!request.isForce() && !getCurrentUser().equals(fileLock.get().getUserId())) {
        throw new FileLockedException(repository.getNamespaceAndName(), fileLock.get());
      }
      svnRepository.unlock(singletonMap(fileToUnlock, fileLock.get().getId()), request.isForce(), null);
    }
  }

  @Override
  public Optional<FileLock> status(LockStatusCommandRequest request) {
    String file = request.getFile();
    try {
      return getFileLock(file, open());
    } catch (SVNException e) {
      throw new InternalRepositoryException(entity("File", file).in(repository), "failed to read lock status", e);
    }
  }

  @Override
  public Collection<FileLock> getAll() {
    try {
      SVNRepository svnRepository = open();
      return Arrays.stream(svnRepository.getLocks("/"))
        .map(this::createLock)
        .collect(toList());
    } catch (SVNException e) {
      throw new InternalRepositoryException(repository, "failed to read locks", e);
    }
  }

  private Optional<FileLock> getFileLock(String file, SVNRepository svnRepository) throws SVNException {
    return ofNullable(svnRepository.getLock(file)).map(this::createLock);
  }

  private FileLock createLock(SVNLock lock) {
    String path = lock.getPath();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return new FileLock(path, lock.getID(), lock.getOwner(), lock.getCreationDate().toInstant());
  }

  private String getCurrentUser() {
    return SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal().toString();
  }
}
