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

package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import java.io.File;
import java.nio.file.Path;

/**
 *
 * @author Sebastian Sdorra
 */
public class TempSCMContextProvider implements SCMContextProvider
{

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public File getBaseDirectory()
  {
    return baseDirectory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Stage getStage()
  {
    return Stage.DEVELOPMENT;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Throwable getStartupError()
  {
    return null;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getVersion()
  {
    return "900.0.1-SNAPSHOT";
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param baseDirectory
   */
  public void setBaseDirectory(File baseDirectory)
  {
    this.baseDirectory = baseDirectory;
  }

  @Override
  public Path resolve(Path path) {
    return baseDirectory.toPath().resolve(path);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File baseDirectory;
}
