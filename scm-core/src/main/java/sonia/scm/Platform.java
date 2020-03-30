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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Locale;

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

    arch = arch.toLowerCase(Locale.ENGLISH);
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
