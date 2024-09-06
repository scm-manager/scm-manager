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

package sonia.scm.util;


import sonia.scm.Platform;


public final class SystemUtil
{

  public static final String PROPERTY_ARCH = "sun.arch.data.model";

  public static final String PROPERTY_OSARCH = "os.arch";

  public static final String PROPERTY_OSNAME = "os.name";

  private static Platform platform =
    new Platform(System.getProperty(PROPERTY_OSNAME),
      System.getProperty(PROPERTY_ARCH), System.getProperty(PROPERTY_OSARCH));


  private SystemUtil() {}


  
  public static boolean is32bit()
  {
    return platform.is32Bit();
  }


  
  public static String getArch()
  {
    return platform.getArch();
  }

  
  public static String getOS()
  {
    return platform.getName();
  }

  public static String getJre() {
    return System.getProperty("java.version");
  }

  
  public static Platform getPlatform()
  {
    return platform;
  }

  
  public static boolean isMac()
  {
    return platform.isMac();
  }

  
  public static boolean isUnix()
  {
    return platform.isUnix();
  }

  
  public static boolean isWindows()
  {
    return platform.isWindows();
  }
}
