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

import lombok.Value;

import java.time.Instant;
import java.util.Optional;

public final class LockCommandBuilder {


  public InnerLockCommandBuilder lock() {
    return new InnerLockCommandBuilder();
  }

  public InnerUnlockCommandBuilder unlock() {
    return null;
  }

  public Optional<FileLock> status(String file) {
    return Optional.empty();
  }

  public class InnerLockCommandBuilder {
    InnerLockCommandBuilder setFile(String file) {
      return this;
    }

    public InnerLockCommandBuilder force(boolean force) {
      return this;
    }

    public LockResult execute() {
      return new LockResult(true);
    }
  }

  public class InnerUnlockCommandBuilder {
    InnerUnlockCommandBuilder setFile(String file) {
      return this;
    }

    public InnerUnlockCommandBuilder force(boolean force) {
      return this;
    }

    public UnlockResult execute() {
      return new UnlockResult(true);
    }
  }

  @Value
  public static class LockResult {
    private boolean successful;
  }

  @Value
  public static class UnlockResult {
    private boolean successful;
  }

  @Value
  public static class FileLock {
    private String userId;
    private Instant timestamp;
  }
}
