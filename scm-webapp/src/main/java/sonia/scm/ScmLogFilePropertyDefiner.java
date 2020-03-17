/**
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

import ch.qos.logback.core.PropertyDefinerBase;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmLogFilePropertyDefiner extends PropertyDefinerBase
{

  /** Field description */
  public static final String LOG_DIRECTORY = "logs";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public ScmLogFilePropertyDefiner()
  {
    File logDirectory = new File(SCMContext.getContext().getBaseDirectory(),
                                 LOG_DIRECTORY);

    if (!logDirectory.exists() &&!logDirectory.mkdirs())
    {
      throw new ConfigurationException(
          "could not create log directory ".concat(logDirectory.getPath()));
    }

    this.logDirectoryPath = logDirectory.getAbsolutePath();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getPropertyValue()
  {
    return logDirectoryPath;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String logDirectoryPath;
}
