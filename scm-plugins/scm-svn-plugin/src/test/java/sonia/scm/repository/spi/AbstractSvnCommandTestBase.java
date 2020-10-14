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

package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.After;

import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class AbstractSvnCommandTestBase extends ZippedRepositoryTestBase
{

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @After
  public void close() throws IOException
  {
    if (context != null)
    {
      context.close();
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public SvnContext createContext()
  {
    if (context == null)
    {
      context = new SvnContext(repository, repositoryDirectory);
    }

    return context;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getType()
  {
    return "svn";
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getZippedRepositoryResource()
  {
    return "sonia/scm/repository/spi/scm-svn-spi-test.zip";
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SvnContext context;
}
