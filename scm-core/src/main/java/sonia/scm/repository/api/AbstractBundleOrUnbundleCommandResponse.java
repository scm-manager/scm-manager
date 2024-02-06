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
    
package sonia.scm.repository.api;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
/**
 * Abstract class for bundle or unbundle command.
 *
 * @since 1.43
 */
public abstract class AbstractBundleOrUnbundleCommandResponse
{
  /** count of bundled/unbundled changesets */
  private final long changesetCount;

  protected AbstractBundleOrUnbundleCommandResponse(long changesetCount)
  {
    this.changesetCount = changesetCount;
  }

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

    final AbstractBundleOrUnbundleCommandResponse other =
      (AbstractBundleOrUnbundleCommandResponse) obj;

    return Objects.equal(changesetCount, other.changesetCount);
  }

 
  @Override
  public int hashCode()
  {
    return Objects.hashCode(changesetCount);
  }

 
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("changesetCount", changesetCount)
                  .toString();
    //J+
  }


  /**
   * Returns the count of bundled/unbundled changesets.
   */
  public long getChangesetCount()
  {
    return changesetCount;
  }

}
