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
