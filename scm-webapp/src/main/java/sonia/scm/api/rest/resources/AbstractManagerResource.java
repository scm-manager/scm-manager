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

import org.apache.commons.beanutils.BeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.LastModifiedAware;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
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
   * Method description
   *
   *
   *
   * @param uriInfo
   * @param item
   *
   * @return
   */
  public Response create(UriInfo uriInfo, T item)
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
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  public Response delete(String name)
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
  public Response update(UriInfo uriInfo, String name, T item)
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
  public Response get(Request request, String id)
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
   * @param start
   * @param limit
   * @param sortby
   * @param desc
   * @return
   */
  public Response getAll(Request request, int start, int limit, String sortby,
                         boolean desc)
  {
    Collection<T> items = fetchItems(sortby, desc, start, limit);

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

  /**
   * Method description
   *
   *
   * @param sortby
   * @param desc
   *
   * @return
   */
  private Comparator<T> createComparator(String sortby, boolean desc)
  {
    Comparator comparator = null;

    if (desc)
    {
      comparator = new BeanReverseComparator(sortby);
    }
    else
    {
      comparator = new BeanComparator(sortby);
    }

    return comparator;
  }

  /**
   * Method description
   *
   *
   *
   * @param sortby
   * @param desc
   * @param start
   * @param limit
   *
   * @return
   */
  private Collection<T> fetchItems(String sortby, boolean desc, int start,
                                   int limit)
  {
    AssertUtil.assertPositive(start);

    Collection<T> items = null;

    if (limit > 0)
    {
      if (Util.isEmpty(sortby))
      {

        // replace with something useful
        sortby = "id";
      }

      items = manager.getAll(createComparator(sortby, desc), start, limit);
    }
    else if (Util.isNotEmpty(sortby))
    {
      items = manager.getAll(createComparator(sortby, desc));
    }
    else
    {
      items = manager.getAll();
    }

    return items;
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

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 11/06/09
   * @author         Enter your name here...
   */
  private static class BeanReverseComparator extends BeanComparator
  {

    /** Field description */
    private static final long serialVersionUID = -8535047820348790009L;

    //~--- constructors -------------------------------------------------------

    /**
     * Constructs ...
     *
     *
     * @param sortby
     */
    private BeanReverseComparator(String sortby)
    {
      super(sortby);
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param o1
     * @param o2
     *
     * @return
     */
    @Override
    public int compare(Object o1, Object o2)
    {
      return super.compare(o1, o2) * -1;
    }
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected int cacheMaxAge = 0;

  /** Field description */
  protected boolean disableCache = false;

  /** Field description */
  protected Manager<T, E> manager;
}
