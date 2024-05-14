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


import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import sonia.scm.Validateable;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.IgnoreWhitespaceLevel;

/**
 *
 * @since 1.17
 */
@EqualsAndHashCode(callSuper = true)
public class DiffCommandRequest extends FileBaseCommandRequest
  implements Validateable {

  private static final long serialVersionUID = 4026911212676859626L;

  private DiffFormat format = DiffFormat.NATIVE;

  private String ancestorChangeset;

  private IgnoreWhitespaceLevel ignoreWhitespace;

  @Override
  public DiffCommandRequest clone()
  {
    DiffCommandRequest clone = null;

    try
    {
      clone = (DiffCommandRequest) super.clone();
    }
    catch (CloneNotSupportedException e)
    {

      // this shouldn't happen, since we are Cloneable
      throw new InternalError("DiffCommandRequest seems not to be cloneable");
    }

    return clone;
  }

  @Override
  public boolean isValid()
  {
    return !Strings.isNullOrEmpty(getPath())
      ||!Strings.isNullOrEmpty(getRevision());
  }

  /**
   * Sets the diff format which should be used for the output.
   *
   *
   * @param format format of the diff output
   *
   * @since 1.34
   */
  public void setFormat(DiffFormat format)
  {
    this.format = format;
  }

  public void setAncestorChangeset(String ancestorChangeset) {
    this.ancestorChangeset = ancestorChangeset;
  }

  /**
   * Return the output format of the diff command.
   * @since 1.34
   */
  public DiffFormat getFormat()
  {
    return format;
  }

  public String getAncestorChangeset() {
    return ancestorChangeset;
  }

  public IgnoreWhitespaceLevel getIgnoreWhitespaceLevel() { return ignoreWhitespace; }

  public void setIgnoreWhitespaceLevel(IgnoreWhitespaceLevel ignoreWhitespace) {
    this.ignoreWhitespace = ignoreWhitespace;
  }

}
