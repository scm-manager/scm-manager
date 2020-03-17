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


package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.Comparator;

/**
 * Compare {@link FileObject}'s by its name.
 *
 * @author Sebastian Sdorra
 */
public class FileObjectNameComparator
  implements Comparator<FileObject>, Serializable
{

  /** Field description */
  public static final FileObjectNameComparator instance =
    new FileObjectNameComparator();

  /** Field description */
  private static final long serialVersionUID = -2133224334287527874L;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param o1
   * @param o2
   *
   * @return
   */
  @Override
  public int compare(FileObject o1, FileObject o2)
  {
    int result;

    if (o1.isDirectory() &&!o2.isDirectory())
    {
      result = -1;
    }
    else if (!o1.isDirectory() && o2.isDirectory())
    {
      result = 1;
    }
    else
    {
      result = Util.compare(o1.getName(), o2.getName());
    }

    return result;
  }
}
