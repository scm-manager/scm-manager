package sonia.scm.lifecycle;

/**
 * Interface for native c library.
 */
@SuppressWarnings({
  "squid:S1214", // usage as constant is common practice for jna
  "squid:S1191"  // use of sun.* classes is required for jna
})
interface CLibrary extends com.sun.jna.Library {
  CLibrary LIBC =  com.sun.jna.Native.load("c", CLibrary.class);

  int F_GETFD = 1;
  int F_SETFD = 2;
  int FD_CLOEXEC = 1;

  int getdtablesize();
  int fcntl(int fd, int command);
  int fcntl(int fd, int command, int flags);
  int execvp(String file, com.sun.jna.StringArray args);
  String strerror(int errno);
}
