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
package sonia.scm.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static sonia.scm.lifecycle.CLibrary.*;

/**
 * Restart strategy which uses execvp from libc. This strategy is only supported on posix base operating systems.
 */
class PosixRestartStrategy extends RestartStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(PosixRestartStrategy.class);

  PosixRestartStrategy() {
  }

  @Override
  protected void executeRestart(InjectionContext context) {
    LOG.warn("restart scm-manager jvm process");
    try {
      restart();
    } catch (IOException e) {
      LOG.error("failed to collect java vm arguments", e);
      LOG.error("we will now exit the java process");
      System.exit(1);
    }
  }

  @SuppressWarnings("squid:S1191") // use of sun.* classes is required for jna)
  private static void restart() throws IOException {
    com.sun.akuma.JavaVMArguments args = com.sun.akuma.JavaVMArguments.current();
    args.remove("--daemon");

    int sz = LIBC.getdtablesize();
    for(int i=3; i<sz; i++) {
      int flags = LIBC.fcntl(i, F_GETFD);
      if(flags<0) continue;
      LIBC.fcntl(i, F_SETFD,flags| FD_CLOEXEC);
    }

    // exec to self
    String exe = args.get(0);
    LIBC.execvp(exe, new com.sun.jna.StringArray(args.toArray(new String[0])));
    throw new IOException("Failed to exec '"+exe+"' "+LIBC.strerror(com.sun.jna.Native.getLastError()));
  }
}
