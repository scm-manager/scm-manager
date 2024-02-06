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
