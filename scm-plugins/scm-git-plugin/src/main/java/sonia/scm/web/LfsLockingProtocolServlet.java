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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sdorra.ssp.PermissionCheck;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import org.apache.shiro.SecurityUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class LfsLockingProtocolServlet extends HttpServlet {

  private static final Logger LOG = LoggerFactory.getLogger(LfsLockingProtocolServlet.class);
  private static final Pattern URL_PATTERN = Pattern.compile(".*\\.git/info/lfs/locks(?:/(verify|(\\w+)/unlock))?");

  private static final int DEFAULT_LIMIT = 1000;
  private static final int LOWER_LIMIT = 10;

  private final Repository repository;
  private final GitLockStore lockStore;
  private final UserDisplayManager userDisplayManager;
  private final ObjectMapper objectMapper;
  private final int defaultLimit;
  private final int lowerLimit;

  public LfsLockingProtocolServlet(Repository repository, GitLockStore lockStore, UserDisplayManager userDisplayManager, ObjectMapper objectMapper) {
    this(repository, lockStore, userDisplayManager, objectMapper, DEFAULT_LIMIT, LOWER_LIMIT);
  }

  LfsLockingProtocolServlet(Repository repository, GitLockStore lockStore, UserDisplayManager userDisplayManager, ObjectMapper objectMapper, int defaultLimit, int lowerLimit) {
    this.repository = repository;
    this.lockStore = lockStore;
    this.userDisplayManager = userDisplayManager;
    this.objectMapper = objectMapper;
    this.defaultLimit = defaultLimit;
    this.lowerLimit = lowerLimit;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    LOG.trace("processing GET request");
    if (!verifyRequest(req, resp, RepositoryPermissions.pull(repository))) {
      return;
    }
    if (!isNullOrEmpty(req.getParameter("path"))) {
      LOG.trace("request limited to path: {}", req.getParameter("path"));
      sendResult(resp, SC_OK, new LocksListDto(lockStore.getLock(req.getParameter("path"))));
    } else if (!isNullOrEmpty(req.getParameter("id"))) {
      LOG.trace("request limited to id: {}", req.getParameter("id"));
      sendResult(resp, SC_OK, new LocksListDto(lockStore.getById(req.getParameter("id"))));
    } else {
      int limit = getLimit(req, resp);
      int cursor = getCursor(req, resp);
      if (limit < 0 || cursor < 0) {
        return;
      }
      Collection<FileLock> allLocks = lockStore.getAll();
      Stream<FileLock> resultLocks = limit(allLocks, limit, cursor);
      LocksListDto result = new LocksListDto(resultLocks, computeNextCursor(limit, cursor, allLocks));
      LOG.trace("created list result with {} locks and next cursor {}", result.getLocks().size(), result.getNextCursor());
      sendResult(resp, SC_OK, result);
    }
  }

  private String computeNextCursor(int limit, int cursor, Collection<FileLock> allLocks) {
    return allLocks.size() > cursor + limit ?Integer.toString(cursor + limit): null;
  }

  private Stream<FileLock> limit(Collection<FileLock> allLocks, int limit, int cursor) {
    return allLocks.stream().skip(cursor).limit(limit);
  }

  private int getLimit(HttpServletRequest req, HttpServletResponse resp) {
    String limitString = req.getParameter("limit");
    if (isNullOrEmpty(limitString)) {
      LOG.trace("using default limit {}", defaultLimit);
      return defaultLimit;
    }
    try {
      return getEffectiveLimit(parseInt(limitString));
    } catch (NumberFormatException e) {
      LOG.trace("illegal limit parameter '{}'", limitString);
      sendError(resp, SC_BAD_REQUEST, "Illegal limit parameter");
      return -1;
    }
  }

  private int getEffectiveLimit(int limit) {
    int effectiveLimit = max(lowerLimit, min(defaultLimit, limit));
    LOG.trace("using limit {}", effectiveLimit);
    return effectiveLimit;
  }

  private int getCursor(HttpServletRequest req, HttpServletResponse resp) {
    String cursor = req.getParameter("cursor");
    return getCursor(resp, cursor);
  }

  private int getCursor(HttpServletResponse resp, String cursor) {
    if (isNullOrEmpty(cursor)) {
      return 0;
    }
    try {
      int effectiveCursor = parseInt(cursor);
      LOG.trace("starting at position {}", effectiveCursor);
      return effectiveCursor;
    } catch (NumberFormatException e) {
      LOG.trace("illegal cursor parameter '{}'", cursor);
      sendError(resp, SC_BAD_REQUEST, "Illegal cursor parameter");
      return -1;
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    LOG.trace("processing POST request");
    if (!verifyRequest(req, resp, RepositoryPermissions.push(repository))) {
      return;
    }
    Matcher matcher = URL_PATTERN.matcher(req.getPathInfo());
    matcher.matches();
    if (matcher.group(1) == null) {
      handleLockRequest(req, resp);
      return;
    }
    String subPath = matcher.group(1);
    if (subPath.equals("verify")) {
      handleVerifyRequest(req, resp);
    } else {
      String lockId = matcher.group(2);
      handleUnlockRequest(req, resp, lockId);
    }
  }

  private void handleLockRequest(HttpServletRequest req, HttpServletResponse resp) {
    LOG.trace("processing lock request");
    readObject(req, resp, LockCreateDto.class).ifPresent(
      lockCreate -> {
        if (isNullOrEmpty(lockCreate.path)) {
          sendError(resp, SC_BAD_REQUEST, "Illegal input");
        } else {
          try {
            FileLock createdLock = lockStore.put(lockCreate.getPath(), false);
            sendResult(resp, SC_CREATED, new SingleLockDto(createdLock));
          } catch (FileLockedException e) {
            FileLock conflictingLock = e.getConflictingLock();
            sendError(resp, SC_CONFLICT, new ConflictDto("already created lock", conflictingLock));
          }
        }
      }
    );
  }

  private void handleVerifyRequest(HttpServletRequest req, HttpServletResponse resp) {
    LOG.trace("processing verify request");
    readObject(req, resp, VerifyDto.class).ifPresent(
      verify -> {
        Collection<FileLock> allLocks = lockStore.getAll();
        int cursor = getCursor(resp, verify.getCursor());
        int limit = getEffectiveLimit(verify.getLimit());
        if (limit < 0 || cursor < 0) {
          return;
        }
        Stream<FileLock> resultLocks = limit(allLocks, limit, cursor);
        VerifyResultDto result = new VerifyResultDto(resultLocks, computeNextCursor(limit, cursor, allLocks));
        LOG.trace("created list result with {} 'our' locks, {} 'their' locks, and next cursor {}", result.getOurs().size(), result.getTheirs().size(), result.getNextCursor());
        sendResult(resp, SC_OK, result);
      }
    );
  }

  private void handleUnlockRequest(HttpServletRequest req, HttpServletResponse resp, String lockId) {
    LOG.trace("processing unlock request");
    readObject(req, resp, UnlockDto.class).ifPresent(
      unlock -> {
        Optional<FileLock> deletedLock = lockStore.removeById(lockId, unlock.isForce());
        if (deletedLock.isPresent()) {
          sendResult(resp, SC_OK, new SingleLockDto(deletedLock.get()));
        } else {
          sendError(resp, SC_NOT_FOUND, "No such lock");
        }
      }
    );
  }

  private <T> Optional<T> readObject(HttpServletRequest req, HttpServletResponse resp, Class<T> resultType) {
    try {
      return ofNullable(objectMapper.readValue(req.getInputStream(), resultType));
    } catch (IOException e) {
      LOG.info("got exception reading input", e);
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
    if (!URL_PATTERN.matcher(req.getPathInfo()).matches()) {
      LOG.trace("got illegal path {}", req.getPathInfo());
      sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "wrong URL for locks api");
      return false;
    }
    return true;
  }

  private void sendResult(HttpServletResponse resp, int statusCode, Object result) {
    LOG.trace("Completing with status code {}", statusCode);
    resp.setStatus(statusCode);
    resp.setContentType("application/vnd.git-lfs+json");
    try {
      objectMapper.writeValue(resp.getOutputStream(), result);
    } catch (IOException e) {
      LOG.warn("Failed to send result to client", e);
    }
  }

  private void sendError(HttpServletResponse resp, int statusCode, String message) {
    LOG.trace("Sending error message '{}'", message);
    sendError(resp, statusCode, new ErrorMessageDto(message));
  }

  private void sendError(HttpServletResponse resp, int statusCode, Object error) {
    LOG.trace("Completing with error, status code {}", statusCode);
    resp.setStatus(statusCode);
    try {
      objectMapper.writeValue(resp.getOutputStream(), error);
    } catch (IOException e) {
      LOG.warn("Failed to send error to client", e);
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

  @Data
  static class VerifyDto {
    private String cursor;
    private int limit = Integer.MAX_VALUE;
  }

  @Data
  static class UnlockDto {
    private boolean force;
  }

  @Value
  private class VerifyResultDto {
    private Collection<LockDto> ours;
    private Collection<LockDto> theirs;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("next_cursor")
    private String nextCursor;

    VerifyResultDto(Stream<FileLock> locks, String nextCursor) {
      String userId = SecurityUtils.getSubject().getPrincipals().oneByType(String.class);
      Map<Boolean, List<LockDto>> groupedLocks = locks.map(LockDto::new).collect(groupingBy(lock -> userId.equals(lock.getUserId())));
      ours = groupedLocks.getOrDefault(true, emptyList());
      theirs = groupedLocks.getOrDefault(false, emptyList());
      this.nextCursor = nextCursor;
    }
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
    @JsonIgnore
    private String userId;

    LockDto(FileLock lock) {
      this.id = lock.getId();
      this.path = lock.getPath();
      this.lockedAt = lock.getTimestamp().toString();
      this.owner = new OwnerDto(lock.getUserId());
      this.userId = lock.getUserId();
    }
  }

  @Getter
  private class LocksListDto {
    private List<LockDto> locks;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("next_cursor")
    private String nextCursor;

    LocksListDto(Optional<FileLock> locks) {
      this.locks = locks.map(LockDto::new).map(Collections::singletonList).orElse(emptyList());
    }

    LocksListDto(Stream<FileLock> locks, String nextCursor) {
      this.locks = locks.map(LockDto::new).collect(toList());
      this.nextCursor = nextCursor;
    }
  }
}
