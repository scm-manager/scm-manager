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

package sonia.scm.installer;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public interface HgInstaller
{

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
  public void install(File baseDirectory, HgConfig config) throws IOException;

  /**
   * Method description
   *
   *
   *
   *
   * @param client
   * @param handler
   * @param baseDirectory
   * @param pkg
   *
   * @return
   */
  public boolean installPackage(AdvancedHttpClient client,
    HgRepositoryHandler handler, File baseDirectory, HgPackage pkg);

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
  public void update(File baseDirectory, HgConfig config) throws IOException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getHgInstallations();

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getPythonInstallations();
}
