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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ModelObject;
import sonia.scm.url.UrlProvider;
import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public abstract class AbstractClientHandler<T extends ModelObject>
        implements ClientHandler<T>
{

  /** the logger for AbstractClientHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractClientHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param session
   * @param itemClass
   */
  public AbstractClientHandler(JerseyClientSession session, Class<T> itemClass)
  {
    this.session = session;
    this.itemClass = itemClass;
    this.client = session.getClient();
    this.urlProvider = session.getUrlProvider();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @return
   */
  protected abstract GenericType<List<T>> createGenericListType();

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param itemId
   *
   * @return
   */
  protected abstract String getItemUrl(String itemId);

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getItemsUrl();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   */
  @Override
  public void create(T item)
  {
    AssertUtil.assertIsNotNull(item);

    WebResource resource = client.resource(getItemsUrl());
    ClientResponse response = null;

    try
    {
      response = resource.post(ClientResponse.class, item);
      ClientUtil.checkResponse(response, 201);

      String url = response.getHeaders().get("Location").get(0);

      AssertUtil.assertIsNotEmpty(url);

      T newItem = getItemByUrl(url);

      AssertUtil.assertIsNotNull(newItem);
      postCreate(response, item, newItem);
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

    WebResource resource = client.resource(getItemUrl(id));
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
   * @param item
   */
  @Override
  public void delete(T item)
  {
    AssertUtil.assertIsNotNull(item);
    delete(item.getId());
  }

  /**
   * Method description
   *
   *
   * @param item
   */
  @Override
  public void modify(T item)
  {
    AssertUtil.assertIsNotNull(item);

    String id = item.getId();

    AssertUtil.assertIsNotEmpty(id);

    WebResource resource = client.resource(getItemUrl(id));
    ClientResponse response = null;

    try
    {
      response = resource.put(ClientResponse.class, item);
      ClientUtil.checkResponse(response, 204);
      postModify(response, item);
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
  public T get(String id)
  {
    return getItemByUrl(getItemUrl(id));
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<T> getAll()
  {
    List<T> items = null;
    String url = getItemsUrl();

    if (logger.isDebugEnabled())
    {
      logger.debug("fetch all items of {} from url", itemClass.getSimpleName(),
                   url);
    }

    WebResource resource = client.resource(url);
    ClientResponse response = null;

    try
    {
      response = resource.get(ClientResponse.class);
      ClientUtil.checkResponse(response, 200);
      items = response.getEntity(createGenericListType());
    }
    finally
    {
      ClientUtil.close(response);
    }

    return items;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param response
   * @param item
   * @param newItem
   */
  protected void postCreate(ClientResponse response, T item, T newItem) {}

  /**
   * Method description
   *
   *
   * @param response
   * @param item
   */
  protected void postModify(ClientResponse response, T item) {}

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   */
  protected T getItemByUrl(String url)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("fetch item {} from url {}", itemClass.getSimpleName(), url);
    }

    T item = null;
    WebResource resource = client.resource(url);
    ClientResponse response = null;

    try
    {
      response = resource.get(ClientResponse.class);

      int sc = response.getStatus();

      if (sc != ScmClientException.SC_NOTFOUND)
      {
        ClientUtil.checkResponse(response, 200);
        item = response.getEntity(itemClass);
      }
    }
    finally
    {
      ClientUtil.close(response);
    }

    return item;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected Client client;

  /** Field description */
  protected JerseyClientSession session;

  /** Field description */
  protected UrlProvider urlProvider;

  /** Field description */
  private Class<T> itemClass;
}
