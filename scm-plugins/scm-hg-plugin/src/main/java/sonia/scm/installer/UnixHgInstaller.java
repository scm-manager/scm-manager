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

package sonia.scm.installer;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.HgConfig;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 * @deprecated use {@link sonia.scm.autoconfig.AutoConfigurator}
 */
@Deprecated
public class UnixHgInstaller extends AbstractHgInstaller
{

  /** Field description */
  public static final String COMMAND_HG = "hg";

  /** Field description */
  public static final String COMMAND_PYTHON = "python";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param baseDirectory
   * @param config
   *
   * @throws IOException
   */
  @Override
  public void install(File baseDirectory, HgConfig config) throws IOException
  {
    // search mercurial (hg)
    if (Util.isEmpty(config.getHgBinary()))
    {
      String hg = IOUtil.search(COMMAND_HG);

      if (Util.isNotEmpty(hg))
      {
        config.setHgBinary(hg);

        // search python in the same folder
        File hgFile = new File(hg);

        if (hgFile.exists())
        {
          File pythonFile = new File(hgFile.getParentFile(), COMMAND_PYTHON);

          if (pythonFile.exists())
          {
            config.setPythonBinary(pythonFile.getAbsolutePath());
          }
        }
      }
    }

    // search python
    if (Util.isEmpty(config.getPythonBinary()))
    {
      config.setPythonBinary(IOUtil.search(COMMAND_PYTHON));
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param baseDirectory
   * @param config
   */
  @Override
  public void update(File baseDirectory, HgConfig config)
  {

    // do nothing
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<String> getHgInstallations()
  {
    return IOUtil.searchAll(COMMAND_HG);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<String> getPythonInstallations()
  {
    return IOUtil.searchAll(COMMAND_PYTHON);
  }
}
