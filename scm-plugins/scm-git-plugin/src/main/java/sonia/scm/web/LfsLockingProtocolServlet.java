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

package sonia.scm.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sdorra.ssp.PermissionCheck;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.TransactionId;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.FileLockedException;
import sonia.scm.repository.spi.GitLockStoreFactory.GitLockStore;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.UserDisplayManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class LfsLockingProtocolServlet extends HttpServlet {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Logger LOG = LoggerFactory.getLogger(LfsLockingProtocolServlet.class);

  private final Repository repository;
  private final GitLockStore lockStore;
  private final UserDisplayManager userDisplayManager;

  public LfsLockingProtocolServlet(Repository repository, GitLockStore lockStore, UserDisplayManager userDisplayManager) {
    this.repository = repository;
    this.lockStore = lockStore;
    this.userDisplayManager = userDisplayManager;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    if (!verifyRequest(req, resp, RepositoryPermissions.pull(repository))) {
      return;
    }
    sendResult(resp, SC_OK, new LocksListDto(lockStore.getAll()));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    if (!verifyRequest(req, resp, RepositoryPermissions.push(repository))) {
      return;
    }
    readObject(req, resp, LockCreateDto.class).ifPresent(
      lockCreate -> {
        try {
          FileLock createdLock = lockStore.put(lockCreate.getPath(), false);
          sendResult(resp, SC_CREATED, new SingleLockDto(createdLock));
        } catch (FileLockedException e) {
          FileLock conflictingLock = e.getConflictingLock();
          sendError(resp, SC_CONFLICT, new ConflictDto("already created lock", conflictingLock));
        }
      }
    );
  }

  private <T> Optional<T> readObject(HttpServletRequest req, HttpServletResponse resp, Class<T> resultType) {
    try {
      return of(OBJECT_MAPPER.readValue(req.getInputStream(), resultType));
    } catch (IOException e) {
      sendError(resp, SC_BAD_REQUEST, "Could not read input");
      return empty();
    }
  }

  private boolean verifyRequest(HttpServletRequest req, HttpServletResponse resp, PermissionCheck permission) {
    return verifyPath(req, resp) && verifyPermission(resp, permission);
  }

  private boolean verifyPermission(HttpServletResponse resp, PermissionCheck permission) {
    if (!permission.isPermitted()) {
      sendError(resp, HttpServletResponse.SC_FORBIDDEN, "You must have push access to create a lock");
      return false;
    }
    return true;
  }

  private boolean verifyPath(HttpServletRequest req, HttpServletResponse resp) {
    if (!req.getPathInfo().matches(".*\\.git/info/lfs/locks(/verify)?")) {
      sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "wrong URL for locks api");
      return false;
    }
    return true;
  }

  private void sendResult(HttpServletResponse resp, int statusCode, Object result) {
    resp.setStatus(statusCode);
    try {
      OBJECT_MAPPER.writeValue(resp.getOutputStream(), result);
    } catch (IOException e) {
      LOG.error("Failed to send result to client", e);
    }
  }

  private void sendError(HttpServletResponse resp, int statusCode, String message) {
    sendError(resp, statusCode, new ErrorMessageDto(message));
  }

  private void sendError(HttpServletResponse resp, int statusCode, Object error) {
    resp.setStatus(statusCode);
    try {
      OBJECT_MAPPER.writeValue(resp.getOutputStream(), error);
    } catch (IOException e) {
      LOG.error("Failed to send error to client", e);
    }
  }

  @Value
  private class ConflictDto extends ErrorMessageDto {
    private LockDto lock;

    public ConflictDto(String message, FileLock lock) {
      super(message);
      this.lock = new LockDto(lock);
    }
  }

  @Getter
  @AllArgsConstructor
  private class ErrorMessageDto {
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String requestId;

    public ErrorMessageDto(String message) {
      this.message = message;
      this.requestId = TransactionId.get().orElse(null);
    }
  }

  @Data
  static class LockCreateDto {
    private String path;
  }

  @Getter
  private class OwnerDto {
    private String name;

    OwnerDto(String userId) {
      this.name = userDisplayManager.get(userId).map(DisplayUser::getDisplayName).orElse(userId);
    }
  }

  @Getter
  private class SingleLockDto {
    private LockDto lock;

    SingleLockDto(FileLock lock) {
      this.lock = new LockDto(lock);
    }
  }

  @Getter
  private class LockDto {
    private String id;
    private String path;
    @JsonProperty("locked_at")
    private String lockedAt;
    private OwnerDto owner;

    LockDto(FileLock lock) {
      this.id = lock.getId();
      this.path = lock.getPath();
      this.lockedAt = lock.getTimestamp().toString();
      this.owner = new OwnerDto(lock.getUserId());
    }
  }

  @Getter
  private class LocksListDto {
    private List<LockDto> locks;

    LocksListDto(Collection<FileLock> locks) {
      this.locks = locks.stream().map(LockDto::new).collect(toList());
    }
  }
}
