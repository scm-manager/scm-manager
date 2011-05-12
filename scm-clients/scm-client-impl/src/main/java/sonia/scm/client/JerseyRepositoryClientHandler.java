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

import sonia.scm.Type;
import sonia.scm.repository.Repository;
import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class JerseyRepositoryClientHandler implements RepositoryClientHandler
{

  /**
   * Constructs ...
   *
   *
   * @param session
   */
  public JerseyRepositoryClientHandler(JerseyClientSession session)
  {
    this.session = session;
    this.client = session.getClient();
    this.urlProvider = session.getUrlProvider();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   */
  @Override
  public void create(Repository repository)
  {
    AssertUtil.assertIsNotNull(repository);

    WebResource resource = client.resource(urlProvider.getRepositoriesUrl());
    ClientResponse response = null;

    try
    {
      response = resource.post(ClientResponse.class, repository);
      ClientUtil.checkResponse(response, 201);

      String url = response.getHeaders().get("Location").get(0);

      AssertUtil.assertIsNotEmpty(url);

      Repository newRepository = getRepository(url);

      AssertUtil.assertIsNotNull(newRepository);
      newRepository.copyProperties(repository);

      // copyProperties does not copy the repository id
      repository.setId(newRepository.getId());
    }
    finally
    {
      ClientUtil.close(response);
    }
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void delete(String id)
  {
    AssertUtil.assertIsNotEmpty(id);

    WebResource resource = client.resource(urlProvider.getRepositoryUrl(id));
    ClientResponse response = null;

    try
    {
      response = resource.delete(ClientResponse.class);
      ClientUtil.checkResponse(response, 204);
    }
    finally
    {
      ClientUtil.close(response);
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   */
  @Override
  public void delete(Repository repository)
  {
    AssertUtil.assertIsNotNull(repository);
    delete(repository.getId());
  }

  /**
   * Method description
   *
   *
   * @param repository
   */
  @Override
  public void modify(Repository repository)
  {
    AssertUtil.assertIsNotNull(repository);

    String id = repository.getId();

    AssertUtil.assertIsNotEmpty(id);

    WebResource resource = client.resource(urlProvider.getRepositoryUrl(id));
    ClientResponse response = null;

    try
    {
      response = resource.post(ClientResponse.class, repository);
      ClientUtil.checkResponse(response, 204);
    }
    finally
    {
      ClientUtil.close(response);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public Repository get(String id)
  {
    return getRepository(urlProvider.getRepositoryUrl(id));
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<Repository> getAll()
  {
    List<Repository> repositories = null;
    WebResource resource = client.resource(urlProvider.getRepositoriesUrl());
    ClientResponse response = null;

    try
    {
      response = resource.get(ClientResponse.class);
      ClientUtil.checkResponse(response, 200);
      repositories = response.getEntity(new GenericType<List<Repository>>() {}
      );
    }
    finally
    {
      ClientUtil.close(response);
    }

    return repositories;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Type> getRepositoryTypes()
  {
    return session.getState().getRepositoryTypes();
  }

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   */
  private Repository getRepository(String url)
  {
    Repository repository = null;
    WebResource resource = client.resource(url);
    ClientResponse response = null;

    try
    {
      response = resource.get(ClientResponse.class);

      int sc = response.getStatus();

      if (sc != ScmClientException.SC_NOTFOUND)
      {
        ClientUtil.checkResponse(response, 200);
        repository = response.getEntity(Repository.class);
      }
    }
    finally
    {
      ClientUtil.close(response);
    }

    return repository;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Client client;

  /** Field description */
  private JerseyClientSession session;

  /** Field description */
  private ScmUrlProvider urlProvider;
}
