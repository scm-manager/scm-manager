/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;

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
    return Objects.toStringHelper(this)
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
