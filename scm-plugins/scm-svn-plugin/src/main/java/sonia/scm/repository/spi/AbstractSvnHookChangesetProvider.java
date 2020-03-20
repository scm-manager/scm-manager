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

import com.google.common.collect.ImmutableSet;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryHookType;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collections;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractSvnHookChangesetProvider
  implements HookChangesetProvider
{

  /**
   * Method description
   *
   *
   * @return
   */
  public abstract RepositoryHookType getType();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract Changeset fetchChangeset();

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @Override
  @SuppressWarnings("unchecked")
  public synchronized HookChangesetResponse handleRequest(
    HookChangesetRequest request)
  {
    if (response == null)
    {
      Changeset c = fetchChangeset();
      Iterable<Changeset> iterable;

      if (c == null)
      {
        iterable = Collections.EMPTY_SET;
      }
      else
      {
        iterable = ImmutableSet.of(c);
      }

      response = new HookChangesetResponse(iterable);
    }

    return response;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HookChangesetResponse response;
}
