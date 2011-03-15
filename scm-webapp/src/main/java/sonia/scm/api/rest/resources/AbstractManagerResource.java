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



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.LastModifiedAware;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 * @param <E>
 */
public abstract class AbstractManagerResource<T extends ModelObject,
        E extends Exception>
{

  /** the logger for AbstractManagerResource */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractManagerResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param manager
   */
  public AbstractManagerResource(Manager<T, E> manager)
  {
    this.manager = manager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param items
   *
   * @return
   */
  protected abstract GenericEntity<Collection<T>> createGenericEntity(
          Collection<T> items);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   *
   * @return
   */
  protected abstract String getId(T item);

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getPathPart();

  //~--- methods --------------------------------------------------------------

  /**
   *  Method description
   *
   *
   *
   * @param uriInfo
   * @param item
   *
   *  @return
   */
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response create(@Context UriInfo uriInfo, T item)
  {
    preCreate(item);

    Response response = null;

    try
    {
      manager.create(item);
      response = Response.created(
        uriInfo.getAbsolutePath().resolve(
          getPathPart().concat("/").concat(getId(item)))).build();
    }
    catch (ScmSecurityException ex)
    {
      logger.warn("create is not allowd", ex);
      response = Response.status(Response.Status.FORBIDDEN).build();
    }
    catch (Exception ex)
    {
      logger.error("error during create", ex);
      response = Response.serverError().build();
    }

    return response;
  }

  /**
   *   Method description
   *
   *
   *   @param name
   *
   *   @return
   */
  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") String name)
  {
    Response response = null;
    T item = manager.get(name);

    if (item != null)
    {
      preDelete(item);

      try
      {
        manager.delete(item);
        response = Response.noContent().build();
      }
      catch (ScmSecurityException ex)
      {
        logger.warn("delete not allowd", ex);
        response = Response.status(Response.Status.FORBIDDEN).build();
      }
      catch (Exception ex)
      {
        logger.error("error during create", ex);
        response = Response.serverError().build();
      }
    }

    return response;
  }

  /**
   *  Method description
   *
   *
   *
   *
   *  @param uriInfo
   *  @param name
   *  @param item
   *
   *
   * @return
   */
  @PUT
  @Path("{id}")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response update(@Context UriInfo uriInfo,
                         @PathParam("id") String name, T item)
  {
    Response response = null;

    preUpate(item);

    try
    {
      manager.modify(item);
      response = Response.noContent().build();
    }
    catch (ScmSecurityException ex)
    {
      logger.warn("delete not allowd", ex);
      response = Response.status(Response.Status.FORBIDDEN).build();
    }
    catch (Exception ex)
    {
      logger.error("error during create", ex);
      response = Response.serverError().build();
    }

    return response;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   *  Method description
   *
   *
   *
   * @param request
   *  @param id
   *
   *  @return
   */
  @GET
  @Path("{id}")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response get(@Context Request request, @PathParam("id") String id)
  {
    Response response = null;
    T item = manager.get(id);

    if (item != null)
    {
      prepareForReturn(item);

      if (disableCache)
      {
        response = Response.ok(item).build();
      }
      else
      {
        response = createCacheResponse(request, item, item);
      }
    }
    else
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }

    return response;
  }

  /**
   * Method description
   *
   *
   *
   * @param request
   * @return
   */
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getAll(@Context Request request)
  {
    Collection<T> items = manager.getAll();

    if (Util.isNotEmpty(items))
    {
      items = prepareForReturn(items);
    }

    Response response = null;
    Object entity = createGenericEntity(items);

    if (disableCache)
    {
      response = Response.ok(entity).build();
    }
    else
    {
      response = createCacheResponse(request, manager, entity);
    }

    return response;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getCacheMaxAge()
  {
    return cacheMaxAge;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isDisableCache()
  {
    return disableCache;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param cacheMaxAge
   */
  public void setCacheMaxAge(int cacheMaxAge)
  {
    this.cacheMaxAge = cacheMaxAge;
  }

  /**
   * Method description
   *
   *
   * @param disableCache
   */
  public void setDisableCache(boolean disableCache)
  {
    this.disableCache = disableCache;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   */
  protected void preCreate(T item) {}

  /**
   * Method description
   *
   *
   * @param item
   */
  protected void preDelete(T item) {}

  /**
   * Method description
   *
   *
   * @param item
   */
  protected void preUpate(T item) {}

  /**
   * Method description
   *
   *
   * @param item
   *
   * @return
   */
  protected T prepareForReturn(T item)
  {
    return item;
  }

  /**
   * Method description
   *
   *
   * @param items
   *
   * @return
   */
  protected Collection<T> prepareForReturn(Collection<T> items)
  {
    return items;
  }

  /**
   * Method description
   *
   *
   * @param rb
   */
  private void addCacheControl(Response.ResponseBuilder rb)
  {
    CacheControl cc = new CacheControl();

    cc.setMaxAge(cacheMaxAge);
    rb.cacheControl(cc);
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param timeItem
   * @param item
   * @param <I>
   *
   * @return
   */
  private <I> Response createCacheResponse(Request request,
          LastModifiedAware timeItem, I item)
  {
    Response.ResponseBuilder builder = null;
    Date lastModified = getLastModified(timeItem);
    EntityTag e = new EntityTag(Integer.toString(item.hashCode()));

    if (lastModified != null)
    {
      builder = request.evaluatePreconditions(lastModified, e);
    }
    else
    {
      builder = request.evaluatePreconditions(e);
    }

    if (builder == null)
    {
      builder = Response.ok(item).tag(e).lastModified(lastModified);
    }

    addCacheControl(builder);

    return builder.build();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   *
   * @return
   */
  private Date getLastModified(LastModifiedAware item)
  {
    Date lastModified = null;
    Long l = item.getLastModified();

    if (l != null)
    {
      lastModified = new Date(l);
    }

    return lastModified;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected int cacheMaxAge = 0;

  /** Field description */
  protected boolean disableCache = false;

  /** Field description */
  protected Manager<T, E> manager;
}
