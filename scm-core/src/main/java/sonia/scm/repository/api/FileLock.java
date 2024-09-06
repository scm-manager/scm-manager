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

package sonia.scm.repository.api;

import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;

/**
 * Detailes of a file lock.
 *
 * @since 2.26.0
 */
public class FileLock implements Serializable {
  private static final long serialVersionUID = 1902345795392347027L;

  private final String path;
  private final String id;
  private final String userId;
  private final Instant timestamp;
  private final String message;

  public FileLock(String path, String id, String userId, Instant timestamp) {
    this(path, id, userId, timestamp, null);
  }

  public FileLock(String path, String id, String userId, Instant timestamp, String message) {
    this.path = path;
    this.id = id;
    this.userId = userId;
    this.timestamp = timestamp;
    this.message = message;
  }

  /**
   * The path of the locked file.
   */
  public String getPath() {
    return path;
  }

  /**
   * The id of the lock.
   */
  public String getId() {
    return id;
  }

  /**
   * The id of the user that created the lock.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * The time the lock was created.
   */
  public Instant getTimestamp() {
    return timestamp;
  }

  public Optional<String> getMessage() {
    return Optional.ofNullable(message);
  }
}
