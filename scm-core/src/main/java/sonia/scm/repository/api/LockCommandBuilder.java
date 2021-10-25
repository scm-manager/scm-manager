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

import sonia.scm.repository.spi.LockCommand;
import sonia.scm.repository.spi.LockCommandRequest;
import sonia.scm.repository.spi.LockStatusCommandRequest;
import sonia.scm.repository.spi.UnlockCommandRequest;

import java.util.Optional;

public final class LockCommandBuilder {

  private final LockCommand lockCommand;

  public LockCommandBuilder(LockCommand lockCommand) {
    this.lockCommand = lockCommand;
  }

  public InnerLockCommandBuilder lock() {
    return new InnerLockCommandBuilder();
  }

  public InnerUnlockCommandBuilder unlock() {
    return new InnerUnlockCommandBuilder();
  }

  public Optional<FileLock> status(String file) {
    LockStatusCommandRequest lockStatusCommandRequest = new LockStatusCommandRequest();
    lockStatusCommandRequest.setFile(file);
    return lockCommand.status(lockStatusCommandRequest);
  }

  public class InnerLockCommandBuilder {
    private String file;
    private boolean force;

    public InnerLockCommandBuilder setFile(String file) {
      this.file = file;
      return this;
    }

    public InnerLockCommandBuilder force(boolean force) {
      this.force = force;
      return this;
    }

    public LockCommandResult execute() {
      LockCommandRequest lockCommandRequest = new LockCommandRequest();
      lockCommandRequest.setFile(file);
      lockCommandRequest.setForce(force);
      return lockCommand.lock(lockCommandRequest);
    }
  }

  public class InnerUnlockCommandBuilder {
    private String file;
    private boolean force;

    public InnerUnlockCommandBuilder setFile(String file) {
      this.file = file;
      return this;
    }

    public InnerUnlockCommandBuilder force(boolean force) {
      this.force = force;
      return this;
    }

    public UnlockCommandResult execute() {
      UnlockCommandRequest unlockCommandRequest = new UnlockCommandRequest();
      unlockCommandRequest.setFile(file);
      unlockCommandRequest.setForce(force);
      return lockCommand.unlock(unlockCommandRequest);
    }
  }

}
