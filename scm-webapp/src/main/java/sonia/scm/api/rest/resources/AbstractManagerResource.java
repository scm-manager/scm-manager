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
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.LastModifiedAware;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.PageResult;
import sonia.scm.api.rest.RestExceptionResult;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

//~--- JDK imports ------------------------------------------------------------

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

  protected final Manager<T, E> manager;
  private final Class<T> type;

  protected int cacheMaxAge = 0;
  protected boolean disableCache = false;

  public AbstractManagerResource(Manager<T, E> manager, Class<T> type) {
    this.manager = manager;
    this.type = type;
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

    Response response;

    try
    {
      manager.create(item);

      String id = getId(item);

      id = HttpUtil.encode(id);
      response = Response.created(
        uriInfo.getAbsolutePath().resolve(
          getPathPart().concat("/").concat(id))).build();
    }
    catch (AuthorizationException ex)
    {
      logger.warn("create is not allowd", ex);
      response = Response.status(Status.FORBIDDEN).build();
    }
    catch (Exception ex)
    {
      logger.error("error during create", ex);
      response = createErrorResponse(ex);
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
      catch (AuthorizationException ex)
      {
        logger.warn("delete not allowd", ex);
        response = Response.status(Response.Status.FORBIDDEN).build();
      }
      catch (Exception ex)
      {
        logger.error("error during delete", ex);
        response = createErrorResponse(ex);
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
   *  @param name
   *  @param item
   *
   *
   * @return
   */
  public Response update(String name, T item)
  {
    Response response = null;

    preUpdate(item);

    try
    {
      manager.modify(item);
      response = Response.noContent().build();
    }
    catch (AuthorizationException ex)
    {
      logger.warn("update not allowed", ex);
      response = Response.status(Response.Status.FORBIDDEN).build();
    }
    catch (Exception ex)
    {
      logger.error("error during update", ex);
      response = createErrorResponse(ex);
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
      response = createCacheResponse(request, manager, items, entity);
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
   * @param throwable
   *
   * @return
   */
  protected Response createErrorResponse(Throwable throwable)
  {
    return createErrorResponse(Status.INTERNAL_SERVER_ERROR,
      throwable.getMessage(), throwable);
  }

  /**
   * Method description
   *
   *
   * @param status
   * @param throwable
   *
   * @return
   */
  protected Response createErrorResponse(Status status, Throwable throwable)
  {
    return createErrorResponse(status, throwable.getMessage(), throwable);
  }

  /**
   * Method description
   *
   *
   * @param status
   * @param message
   * @param throwable
   *
   * @return
   */
  protected Response createErrorResponse(Status status, String message,
    Throwable throwable)
  {
    return Response.status(status).entity(new RestExceptionResult(message,
      throwable)).build();
  }

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
  protected void preUpdate(T item) {}

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
    return createCacheResponse(request, timeItem, item, item);
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param timeItem
   * @param entityItem
   * @param item
   * @param <I>
   *
   * @return
   */
  private <I> Response createCacheResponse(Request request,
    LastModifiedAware timeItem, Object entityItem, I item)
  {
    Response.ResponseBuilder builder = null;
    Date lastModified = getLastModified(timeItem);
    EntityTag e = new EntityTag(Integer.toString(entityItem.hashCode()));

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

  @SuppressWarnings("unchecked")
  private Comparator<T> createComparator(String sortBy, boolean desc)
  {
    checkSortByField(sortBy);
    Comparator comparator;

    if (desc)
    {
      comparator = new BeanReverseComparator(sortBy);
    }
    else
    {
      comparator = new BeanComparator(sortBy);
    }

    return comparator;
  }

  private Collection<T> fetchItems(String sortBy, boolean desc, int start,
    int limit)
  {
    AssertUtil.assertPositive(start);

    Collection<T> items = null;

    if (limit > 0)
    {
      if (Util.isEmpty(sortBy))
      {

        // replace with something useful
        sortBy = "id";
      }

      items = manager.getAll(createComparator(sortBy, desc), start, limit);
    }
    else if (Util.isNotEmpty(sortBy))
    {
      items = manager.getAll(createComparator(sortBy, desc));
    }
    else
    {
      items = manager.getAll();
    }

    return items;
  }

  // We have to handle IntrospectionException here, because it's a checked exception
  // It shouldn't occur really - so creating a new unchecked exception would be over-engineered here
  @SuppressWarnings("squid:S00112")
  private void checkSortByField(String sortBy) {
    try {
      BeanInfo info = Introspector.getBeanInfo(type);
      PropertyDescriptor[] pds = info.getPropertyDescriptors();
      if (Arrays.stream(pds).noneMatch(p -> p.getName().equals(sortBy))) {
        throw new IllegalArgumentException("sortBy");
      }
    } catch (IntrospectionException e) {
      throw new RuntimeException("error introspecting model type " + type.getName(), e);
    }
  }

  protected PageResult<T> fetchPage(String sortBy, boolean desc, int pageNumber,
    int pageSize) {
    AssertUtil.assertPositive(pageNumber);
    AssertUtil.assertPositive(pageSize);

    if (Util.isEmpty(sortBy)) {
      // replace with something useful
      sortBy = "id";
    }

    return manager.getPage(createComparator(sortBy, desc), pageNumber, pageSize);
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
    @SuppressWarnings("unchecked")
    public int compare(Object o1, Object o2)
    {
      return super.compare(o1, o2) * -1;
    }
  }
}
