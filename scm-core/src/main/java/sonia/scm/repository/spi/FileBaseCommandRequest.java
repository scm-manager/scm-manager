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


import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 *
 * @since 1.17
 */
@EqualsAndHashCode
@ToString
public abstract class FileBaseCommandRequest
  implements Resetable, Serializable, Cloneable {

  private static final long serialVersionUID = -3442101119408346165L;

   @Override
  public void reset()
  {
    path = null;
    revision = null;
  }

  public void setPath(String path)
  {
    this.path = path;
  }

  public void setRevision(String revision)
  {
    this.revision = revision;
  }

  public String getPath()
  {
    return path;
  }

  public String getRevision()
  {
    return revision;
  }

  @Override
  protected FileBaseCommandRequest clone() throws CloneNotSupportedException
  {
    FileBaseCommandRequest clone = null;

    try
    {
      clone = (FileBaseCommandRequest) super.clone();
    }
    catch (CloneNotSupportedException e)
    {

      // this shouldn't happen, since we are Cloneable
      throw new InternalError(
        "FileBaseCommandRequest seems not to be cloneable");
    }

    return clone;
  }

  private String path;

  private String revision;
}
