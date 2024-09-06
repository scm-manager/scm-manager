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

package sonia.scm;

import java.util.Locale;

/**
 * Type of the SCM-Manager host platform.
 *
 */
public enum PlatformType
{
  UNSPECIFIED(false, false), MAC(true, true), LINUX(false, true),
  WINDOWS(false, false), SOLARIS(true, true), FREEBSD(true, true),
  OPENBSD(true, true);

  /** has the platform support for posix */
  private boolean posix;

  /** is the platform a unix system */
  private boolean unix;

  /**
   * Constructs {@link PlatformType} object.
   *
   *
   * @param unix - unix operating system
   * @param posix - support for posix
   */
  private PlatformType(boolean unix, boolean posix)
  {
    this.unix = unix;
    this.posix = posix;
  }


  /**
   * Returns {@link PlatformType} object for the given operating system name.
   *
   *
   * @param osName - name of the operating system
   */
  public static PlatformType createPlatformType(String osName)
  {
    osName = osName.toLowerCase(Locale.ENGLISH);

    PlatformType type = PlatformType.UNSPECIFIED;

    if (osName.startsWith("linux"))
    {
      type = PlatformType.LINUX;
    }
    else if (osName.startsWith("mac") || osName.startsWith("darwin"))
    {
      type = PlatformType.MAC;
    }
    else if (osName.startsWith("windows"))
    {
      type = PlatformType.WINDOWS;
    }
    else if (osName.startsWith("solaris") || osName.startsWith("sunos"))
    {
      type = PlatformType.SOLARIS;
    }
    else if (osName.startsWith("freebsd"))
    {
      type = PlatformType.FREEBSD;
    }
    else if (osName.startsWith("openbsd"))
    {
      type = PlatformType.OPENBSD;
    }

    return type;
  }


  /**
   * Returns true if the platform has support for posix.
   *
   *
   * @return true if the platform has support for posix
   */
  public boolean isPosix()
  {
    return posix;
  }

  /**
   * Returns true if the platform is a unix system.
   *
   *
   * @return true if the platform is a unix system
   */
  public boolean isUnix()
  {
    return unix;
  }

}
