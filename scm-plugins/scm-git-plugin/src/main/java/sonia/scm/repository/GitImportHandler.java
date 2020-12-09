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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sebastian Sdorra
 * @deprecated
 */
@Deprecated
public class GitImportHandler extends AbstactImportHandler {

  /**
   * Field description
   */
  public static final String GIT_DIR = ".git";

  /**
   * Field description
   */
  public static final String GIT_DIR_REFS = "refs";

  /**
   * the logger for GitImportHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GitImportHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param handler
   */
  public GitImportHandler(GitRepositoryHandler handler) {
    this.handler = handler;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @return
   */
  @Override
  protected String[] getDirectoryNames() {
    return new String[]{GIT_DIR, GIT_DIR_REFS};
  }

  /**
   * Method description
   *
   * @return
   */
  @Override
  protected AbstractRepositoryHandler<?> getRepositoryHandler() {
    return handler;
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * Field description
   */
  private GitRepositoryHandler handler;
}
