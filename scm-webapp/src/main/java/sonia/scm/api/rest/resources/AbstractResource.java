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

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public abstract class AbstractResource<T>
{

  /**
   * Method description
   *
   *
   * @param item
   *
   * @throws Exception
   */
  protected abstract void addItem(T item) throws Exception;

  /**
   * Method description
   *
   *
   * @param item
   *
   * @throws Exception
   */
  protected abstract void removeItem(T item) throws Exception;

  /**
   * Method description
   *
   *
   * @param name
   * @param item
   *
   * @throws Exception
   */
  protected abstract void updateItem(String name, T item) throws Exception;

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract Collection<T> getAllItems();

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
   * @param name
   *
   * @return
   */
  protected abstract T getItem(String name);

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
  public Response add(@Context UriInfo uriInfo, T item)
  {
    try
    {
      addItem(item);
    }
    catch (Exception ex)
    {
      throw new WebApplicationException(ex);
    }

    return Response.created(
        uriInfo.getAbsolutePath().resolve(
          getPathPart().concat("/").concat(getId(item)))).build();
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
  @Path("{name}")
  public Response delete(@PathParam("name") String name)
  {
    T item = getItem(name);

    if (item == null)
    {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    try
    {
      removeItem(item);
    }
    catch (Exception ex)
    {
      throw new WebApplicationException(ex);
    }

    return Response.noContent().build();
  }

  /**
   * Method description
   *
   *
   *
   *
   * @param uriInfo
   * @param name
   * @param item
   *
   */
  @PUT
  @Path("{name}")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public void update(@Context UriInfo uriInfo, @PathParam("name") String name,
                     T item)
  {
    try
    {
      updateItem(name, item);
    }
    catch (Exception ex)
    {
      throw new WebApplicationException(ex);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  @GET
  @Path("{name}")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public T get(@PathParam("name") String name)
  {
    T item = getItem(name);

    if (item == null)
    {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    return item;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Collection<T> getAll()
  {
    return getAllItems();
  }
}
