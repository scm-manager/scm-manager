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

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.tmatesoft.svn.core.auth.BasicAuthenticationManager.newInstance;
import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class SvnFileLockCommand extends AbstractSvnCommand implements FileLockCommand {

  private static final String LOCK_MESSAGE_PREFIX = "locked by SCM-Manager for ";

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
    String currentUser = initializeAuthentication(svnRepository);
    getFileLock(fileToLock, svnRepository)
      .ifPresent(lock -> {
        throw new FileLockedException(repository.getNamespaceAndName(), lock);
      });
    svnRepository.lock(singletonMap(fileToLock, null), LOCK_MESSAGE_PREFIX + currentUser, false, null);
  }

  @Override
  public UnlockCommandResult unlock(UnlockCommandRequest request) {
    return unlock(request, any -> true);
  }

  public UnlockCommandResult unlock(UnlockCommandRequest request, Predicate<SvnFileLock> predicate) {
    String fileToUnlock = request.getFile();
    try {
      doUnlock(request, predicate);
      return new UnlockCommandResult(true);
    } catch (SVNException e) {
      throw new InternalRepositoryException(entity("File", fileToUnlock).in(repository), "failed to unlock file", e);
    }
  }

  private void doUnlock(UnlockCommandRequest request, Predicate<SvnFileLock> predicate) throws SVNException {
    String fileToUnlock = request.getFile();
    SVNRepository svnRepository = open();
    initializeAuthentication(svnRepository);
    Optional<SvnFileLock> fileLock = getFileLock(fileToUnlock, svnRepository);
    if (fileLock.isPresent()) {
      SvnFileLock lock = fileLock.get();
      if (shouldPreventUnlock(request, predicate, lock)) {
        throw new FileLockedException(repository.getNamespaceAndName(), lock);
      }
      svnRepository.unlock(singletonMap(fileToUnlock, lock.getId()), request.isForce(), null);
    }
  }

  @Override
  public Optional<FileLock> status(LockStatusCommandRequest request) {
    String file = request.getFile();
    try {
      return getFileLock(file, open()).map(lock -> lock);
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

  private Optional<SvnFileLock> getFileLock(String file, SVNRepository svnRepository) throws SVNException {
    return ofNullable(svnRepository.getLock(file)).map(this::createLock);
  }

  private SvnFileLock createLock(SVNLock lock) {
    String path = lock.getPath();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return new SvnFileLock(path, lock.getID(), lock.getOwner(), lock.getCreationDate().toInstant(), lock.getComment());
  }

  private boolean shouldPreventUnlock(UnlockCommandRequest request, Predicate<SvnFileLock> predicate, SvnFileLock lock) {
    return !request.isForce() && !getCurrentUser().equals(lock.getUserId()) || !predicate.test(lock);
  }

  private String initializeAuthentication(SVNRepository svnRepository) {
    String currentUser = getCurrentUser();
    ISVNAuthenticationManager authenticationManager = newInstance(currentUser, null);
    svnRepository.setAuthenticationManager(authenticationManager);
    return currentUser;
  }

  private String getCurrentUser() {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

  static class SvnFileLock extends FileLock {
    private SvnFileLock(String path, String id, String userId, Instant timestamp, String message) {
      super(path, id, userId, timestamp, message);
    }

    boolean isCreatedByScmManager() {
      return getMessage().filter(message -> message.startsWith(LOCK_MESSAGE_PREFIX)).isPresent();
    }
  }
}
