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



package sonia.scm.client;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 *
 * @author Sebastian Sdorra
 */
public class JerseyClientChangesetHandler implements ClientChangesetHandler
{

  /**
   * Constructs ...
   *
   *
   * @param session
   * @param repository
   */
  public JerseyClientChangesetHandler(JerseyClientSession session,
          Repository repository)
  {
    this.session = session;
    this.repository = repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   *
   * @return
   */
  @Override
  public Changeset getChangeset(String revision)
  {
    Changeset changeset = null;
    String url =
      session.getUrlProvider().getRepositoryUrlProvider().getChangesetUrl(
          repository.getId(), revision);
    WebResource resource = session.getClient().resource(url);
    ClientResponse response = null;

    try
    {
      response = resource.get(ClientResponse.class);

      if (response.getStatus() != ScmClientException.SC_NOTFOUND)
      {
        ClientUtil.checkResponse(response, 200);
        changeset = response.getEntity(Changeset.class);
      }
    }
    finally
    {
      ClientUtil.close(response);
    }

    return changeset;
  }

  /**
   * Method description
   *
   *
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public ChangesetPagingResult getChangesets(int start, int limit)
  {
    ChangesetPagingResult result = null;
    String url =
      session.getUrlProvider().getRepositoryUrlProvider().getChangesetUrl(
          repository.getId(), start, limit);
    WebResource resource = session.getClient().resource(url);
    ClientResponse response = null;

    try
    {
      response = resource.get(ClientResponse.class);

      if (response.getStatus() != ScmClientException.SC_NOTFOUND)
      {
        ClientUtil.checkResponse(response, 200);
        result = response.getEntity(ChangesetPagingResult.class);
      }
    }
    finally
    {
      ClientUtil.close(response);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   *
   * @param path
   * @param revision
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public ChangesetPagingResult getChangesets(String path, String revision,
          int start, int limit)
  {
    ChangesetPagingResult result = null;
    String url =
      session.getUrlProvider().getRepositoryUrlProvider().getChangesetUrl(
          repository.getId(), path, revision, start, limit);
    WebResource resource = session.getClient().resource(url);
    ClientResponse response = null;

    try
    {
      response = resource.get(ClientResponse.class);

      if (response.getStatus() != ScmClientException.SC_NOTFOUND)
      {
        ClientUtil.checkResponse(response, 200);
        result = response.getEntity(ChangesetPagingResult.class);
      }
    }
    finally
    {
      ClientUtil.close(response);
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Repository repository;

  /** Field description */
  private JerseyClientSession session;
}
