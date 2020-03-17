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


package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import sonia.scm.repository.Repository;

import java.net.URL;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
public abstract class RemoteCommandRequest implements Resetable
{

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

    final RemoteCommandRequest other = (RemoteCommandRequest) obj;

    return Objects.equal(remoteRepository, other.remoteRepository)
      && Objects.equal(remoteUrl, other.remoteUrl);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(remoteRepository, remoteUrl);
  }

  /**
   * Resets the request object.
   *
   * @since 1.43
   */
  @Override
  public void reset()
  {
    remoteRepository = null;
    remoteUrl = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("remoteRepository", remoteRepository)
                  .add("remoteUrl", remoteUrl)
                  .toString();
    //J+
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @param remoteRepository
   */
  public void setRemoteRepository(Repository remoteRepository)
  {
    this.remoteRepository = remoteRepository;
  }

  /**
   * Method description
   *
   *
   * @param remoteUrl
   *
   * @since 1.43
   */
  public void setRemoteUrl(URL remoteUrl)
  {
    this.remoteUrl = remoteUrl;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  Repository getRemoteRepository()
  {
    return remoteRepository;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @since 1.43
   */
  URL getRemoteUrl()
  {
    return remoteUrl;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected Repository remoteRepository;

  /** remote url */
  protected URL remoteUrl;
}
