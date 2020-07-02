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

package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.SCMContextProvider;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 */
public enum HgPythonScript {

  HOOK("scmhooks.py"), HGWEB("hgweb.py"), VERSION("version.py");

  /** Field description */
  private static final String BASE_DIRECTORY =
    "lib".concat(File.separator).concat("python");

  /** Field description */
  private static final String BASE_RESOURCE = "/sonia/scm/python/";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param name
   */
  private HgPythonScript(String name)
  {
    this.name = name;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param context
   *
   * @return
   */
  public static File getScriptDirectory(SCMContextProvider context)
  {
    return new File(context.getBaseDirectory(), BASE_DIRECTORY);
  }

  /**
   * Method description
   *
   *
   * @param context
   *
   * @return
   */
  public File getFile(SCMContextProvider context)
  {
    return new File(getScriptDirectory(context), name);
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
  public String getResourcePath()
  {
    return BASE_RESOURCE.concat(name);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String name;
}
