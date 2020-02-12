package sonia.scm.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static sonia.scm.lifecycle.CLibrary.*;

/**
 * Restart strategy which uses execvp from libc. This strategy is only supported on posix base operating systems.
 */
class PosixRestartStrategy implements RestartStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(PosixRestartStrategy.class);

  PosixRestartStrategy() {
  }

  @Override
  public void restart(InjectionContext context) {
    LOG.warn("destroy injection context");
    context.destroy();

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
