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
