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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Platform;

/**
 *
 * @author Sebastian Sdorra
 */
public final class SystemUtil
{

  /** Field description */
  public static final String PROPERTY_ARCH = "sun.arch.data.model";

  /** Field description */
  public static final String PROPERTY_OSARCH = "os.arch";

  /** Field description */
  public static final String PROPERTY_OSNAME = "os.name";

  /** Field description */
  private static Platform platform =
    new Platform(System.getProperty(PROPERTY_OSNAME),
      System.getProperty(PROPERTY_ARCH), System.getProperty(PROPERTY_OSARCH));

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private SystemUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static boolean is32bit()
  {
    return platform.is32Bit();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static String getArch()
  {
    return platform.getArch();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static String getOS()
  {
    return platform.getName();
  }

  public static String getJre() {
    return System.getProperty("java.version");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static Platform getPlatform()
  {
    return platform;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static boolean isMac()
  {
    return platform.isMac();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static boolean isUnix()
  {
    return platform.isUnix();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static boolean isWindows()
  {
    return platform.isWindows();
  }
}
