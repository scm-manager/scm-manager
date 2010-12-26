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

/**
 *
 * @author Sebastian Sdorra
 */
public class SystemUtil
{

  /** Field description */
  public static final String PROPERTY_OSNAME = "os.name";

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static boolean isMac()
  {
    String os = System.getProperty(PROPERTY_OSNAME).toLowerCase();

    // Mac
    return (os.indexOf("mac") >= 0);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static boolean isUnix()
  {
    String os = System.getProperty(PROPERTY_OSNAME).toLowerCase();

    // linux or unix
    return ((os.indexOf("nix") >= 0) || (os.indexOf("nux") >= 0));
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static boolean isWindows()
  {
    String os = System.getProperty(PROPERTY_OSNAME).toLowerCase();

    // windows
    return (os.indexOf("win") >= 0);
  }

  public static boolean is32bit()
  {
    return "32".equals(System.getProperty("sun.arch.data.model"));
  }
}
