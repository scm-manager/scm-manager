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
 *
 * @author Sebastian Sdorra
 */
public class Platform
{

  /**
   * Constructs ...
   *
   *
   * @param osName
   * @param archModel
   * @param osArch
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

    if (osName.startsWith("Linux"))
    {
      type = PlatformType.LINUX;
    }
    else if (osName.startsWith("Mac") || osName.startsWith("Darwin"))
    {
      type = PlatformType.MAC;
    }
    else if (osName.startsWith("Windows"))
    {
      type = PlatformType.WINDOWS;
    }
    else if (osName.startsWith("Solaris") || osName.startsWith("SunOS"))
    {
      type = PlatformType.SOLARIS;
    }
    else if (osName.startsWith("FreeBSD"))
    {
      type = PlatformType.FREEBSD;
    }
    else if (osName.startsWith("OpenBSD"))
    {
      type = PlatformType.OPENBSD;
    }
    else
    {
      type = PlatformType.UNSPECIFIED;
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean is32Bit()
  {
    return !x64;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean is64Bit()
  {
    return x64;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getArch()
  {
    return arch;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public PlatformType getType()
  {
    return type;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isFreeBSD()
  {
    return PlatformType.FREEBSD == type;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isLinux()
  {
    return PlatformType.LINUX == type;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isMac()
  {
    return PlatformType.MAC == type;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isOpenBSD()
  {
    return PlatformType.OPENBSD == type;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isPosix()
  {
    return type.isPosix();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isSolaris()
  {
    return PlatformType.SOLARIS == type;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isUnix()
  {
    return type.isUnix();
  }

  /**
   * Method description
   *
   *
   * @return
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
