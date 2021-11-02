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

package sonia.scm.repository.api;

import java.io.Serializable;
import java.time.Instant;

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

  public FileLock(String path, String id, String userId, Instant timestamp) {
    this.path = path;
    this.id = id;
    this.userId = userId;
    this.timestamp = timestamp;
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
}
