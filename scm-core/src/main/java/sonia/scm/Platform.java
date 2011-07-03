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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

/**
 * Represents the platform on which the SCM manager running.
 *
 * @author Sebastian Sdorra
 */
public class Platform
{

  /**
   * Constructs a {@link Platform} object
   *
   *
   * @param osName - name of the operation system
   * @param archModel - name of the host architecture model
   * @param osArch - name of the operation system architecture
   */
  public Platform(String osName, String archModel, String osArch)
  {
    this.name = osName;

    if (Util.isNotEmpty(archModel))
    {
      arch = archModel;
    }
    else
    {
      arch = osArch;
    }

    arch = arch.toLowerCase();
    x64 = "64".equals(arch) || "x86_64".equals(arch) || "ppc64".equals(arch)
          || "sparcv9".equals(arch) || "amd64".equals(arch);
    type = PlatformType.createPlatformType(osName);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns true if the operating system is a 32 bit operating system.
   *
   *
   * @return true if the operating system is a 32 bit operating system
   */
  public boolean is32Bit()
  {
    return !x64;
  }

  /**
   * Returns true if the operating system is a 64 a bit operating system.
   *
   *
   * @return true if the operating system is a 64 a bit operating system
   */
  public boolean is64Bit()
  {
    return x64;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the architecture of the platform.
   *
   *
   * @return the architecture of the platform
   */
  public String getArch()
  {
    return arch;
  }

  /**
   * Returns the name of the platform.
   *
   *
   * @return name of the platform
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the type of the platform.
   *
   *
   * @return type of the platform
   */
  public PlatformType getType()
  {
    return type;
  }

  /**
   * Returns true if the operating system is a FreeBSD.
   *
   *
   * @return true if the operating system is a FreeBSD
   */
  public boolean isFreeBSD()
  {
    return PlatformType.FREEBSD == type;
  }

  /**
   * Returns true if the operating system is a Linux.
   *
   *
   * @return true if the operating system is a Linux
   */
  public boolean isLinux()
  {
    return PlatformType.LINUX == type;
  }

  /**
   * Returns true if the operating system is a Mac OS.
   *
   *
   * @return true if the operating system is a Mac OS
   */
  public boolean isMac()
  {
    return PlatformType.MAC == type;
  }

  /**
   * Returns true if the operating system is a OpenBSD.
   *
   *
   * @return true if the operating system is a OpenBSD
   */
  public boolean isOpenBSD()
  {
    return PlatformType.OPENBSD == type;
  }

  /**
   * Returns true if the operating system has posix support.
   *
   *
   * @return true if the operating system has posix support
   */
  public boolean isPosix()
  {
    return type.isPosix();
  }

  /**
   * Returns true if the operating system is a Solaris.
   *
   *
   * @return true if the operating system is a Solaris
   */
  public boolean isSolaris()
  {
    return PlatformType.SOLARIS == type;
  }

  /**
   * Returns true if the operating system is a Unix system.
   *
   *
   * @return true if the operating system is a Unix system
   */
  public boolean isUnix()
  {
    return type.isUnix();
  }

  /**
   * Returns true if the operating system is a Windows.
   *
   *
   * @return true if the operating system is a Windows
   */
  public boolean isWindows()
  {
    return PlatformType.WINDOWS == type;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String arch;

  /** Field description */
  private String name;

  /** Field description */
  private PlatformType type;

  /** Field description */
  private boolean x64;
}
