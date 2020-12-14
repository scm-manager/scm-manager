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

import com.google.common.base.Objects;
import com.google.common.io.ByteSource;

/**
 * Request object for the unbundle command.
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
 * @since 1.43
 */
public final class UnbundleCommandRequest
{

  /**
   * Constructs a new unbundle command request.
   *
   *
   * @param archive byte source archive
   */
  public UnbundleCommandRequest(ByteSource archive)
  {
    this.archive = archive;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final UnbundleCommandRequest other = (UnbundleCommandRequest) obj;

    return Objects.equal(archive, other.archive);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(archive);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the archive as {@link ByteSource}.
   *
   *
   * @return {@link ByteSource} archive
   */
  public ByteSource getArchive()
  {
    return archive;
  }

  //~--- fields ---------------------------------------------------------------

  /** byte source archive */
  private final ByteSource archive;
}
