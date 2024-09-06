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


import sonia.scm.util.Util;

import java.util.Locale;

/**
 * Represents the platform on which the SCM manager running.
 *
 */
public class Platform
{

  private String arch;

  private String name;

  private PlatformType type;

  private boolean x64;
  /**
   * Constructs a {@link Platform} object
   *
   *
   * @param osName - name of the operating system
   * @param archModel - name of the host architecture model
   * @param osArch - name of the operating system architecture
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


  /**
   * Returns true if the operating system is a 32-bit operating system.
   */
  public boolean is32Bit()
  {
    return !x64;
  }

  /**
   * Returns true if the operating system is a 64 a bit operating system.
   */
  public boolean is64Bit()
  {
    return x64;
  }


  /**
   * Returns the architecture of the platform.
   */
  public String getArch()
  {
    return arch;
  }

  /**
   * Returns the name of the platform.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the type of the platform.
   */
  public PlatformType getType()
  {
    return type;
  }

  /**
   * Returns true if the operating system is a FreeBSD.
   */
  public boolean isFreeBSD()
  {
    return PlatformType.FREEBSD == type;
  }

  /**
   * Returns true if the operating system is a Linux.
   */
  public boolean isLinux()
  {
    return PlatformType.LINUX == type;
  }

  /**
   * Returns true if the operating system is a Mac OS.
   */
  public boolean isMac()
  {
    return PlatformType.MAC == type;
  }

  /**
   * Returns true if the operating system is a OpenBSD.
   */
  public boolean isOpenBSD()
  {
    return PlatformType.OPENBSD == type;
  }

  /**
   * Returns true if the operating system has posix support.
   */
  public boolean isPosix()
  {
    return type.isPosix();
  }

  /**
   * Returns true if the operating system is a Solaris.
   */
  public boolean isSolaris()
  {
    return PlatformType.SOLARIS == type;
  }

  /**
   * Returns true if the operating system is a Unix system.
   */
  public boolean isUnix()
  {
    return type.isUnix();
  }

  /**
   * Returns true if the operating system is a Windows.
   */
  public boolean isWindows()
  {
    return PlatformType.WINDOWS == type;
  }

}
