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

//~--- JDK imports ------------------------------------------------------------

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * Abstract base class for {@link PushResponse} and {@link PullResponse}.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractPushOrPullResponse
{

  /**
   * Constructs a new AbstractPushOrPullResponse.
   *
   */
  public AbstractPushOrPullResponse() {}

  /**
   * Constructs a new AbstractPushOrPullResponse.
   *
   * @param changesetCount count of pushed/pulled changesets
   */
  public AbstractPushOrPullResponse(long changesetCount)
  {
    this.changesetCount = changesetCount;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the count of pushed/pulled changesets.
   *
   *
   * @return count of pushed/pulled changesets
   */
  public long getChangesetCount()
  {
    return changesetCount;
  }

  //~--- fields ---------------------------------------------------------------

  /** count of pushed/pulled changesets */
  @XmlElement(name = "changeset-count")
  private long changesetCount;
}
