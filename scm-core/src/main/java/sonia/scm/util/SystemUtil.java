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



package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Platform;
import sonia.scm.ServletContainer;
import sonia.scm.ServletContainerDetector;

/**
 *
 * @author Sebastian Sdorra
 */
public class SystemUtil
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
                 System.getProperty(PROPERTY_ARCH),
                 System.getProperty(PROPERTY_OSARCH));

  /** Field description */
  private static ServletContainer servletContainer =
    ServletContainerDetector.detect();

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
  public static ServletContainer getServletContainer()
  {
    return servletContainer;
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
