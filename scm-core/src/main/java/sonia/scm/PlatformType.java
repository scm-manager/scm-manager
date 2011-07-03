/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm;

/**
 * Type of the SCM-Manager host platform.
 *
 * @author Sebastian Sdorra
 */
public enum PlatformType
{
  UNSPECIFIED(false, false), MAC(true, true), LINUX(false, true),
  WINDOWS(false, false), SOLARIS(true, true), FREEBSD(true, true),
  OPENBSD(true, true);

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

  //~--- methods --------------------------------------------------------------

  /**
   * Returns {@link PlatformType} object for the given operating system name.
   *
   *
   * @param osName - name of the operating system
   *
   * @return {@link PlatformType} object for the given operating system name
   */
  public static PlatformType createPlatformType(String osName)
  {
    osName = osName.toLowerCase();

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

  //~--- get methods ----------------------------------------------------------

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

  //~--- fields ---------------------------------------------------------------

  /** has the platform support for posix */
  private boolean posix;

  /** is the platform is a unix system */
  private boolean unix;
}
