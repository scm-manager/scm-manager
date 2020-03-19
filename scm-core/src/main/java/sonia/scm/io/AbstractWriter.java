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

package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public abstract class AbstractWriter<T>
{

  /**
   * Method description
   *
   *
   * @param object
   * @param output
   *
   * @throws IOException
   */
  public abstract void write(T object, OutputStream output) throws IOException;

  /**
   * Method description
   *
   *
   * @param object
   * @param file
   *
   * @throws IOException
   */
  public void write(T object, File file) throws IOException
  {
    OutputStream output = null;

    try
    {
      output = new FileOutputStream(file);
      write(object, output);
    }
    finally
    {
      IOUtil.close(output);
    }
  }

  /**
   * Method description
   *
   *
   * @param object
   * @param path
   *
   * @throws IOException
   */
  public void write(T object, String path) throws IOException
  {
    write(object, new File(path));
  }
}
