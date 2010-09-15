/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- JDK imports ------------------------------------------------------------

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
  protected abstract T[] getAllItems();

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
   * @return
   */
  @PUT
  @Path("{name}")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response update(@Context UriInfo uriInfo,
                         @PathParam("name") String name, T item)
  {
    try
    {
      updateItem(name, item);
    }
    catch (Exception ex)
    {
      throw new WebApplicationException(ex);
    }

    return Response.created(
        uriInfo.getAbsolutePath().resolve(getId(item))).build();
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
  public T[] getAll()
  {
    return getAllItems();
  }
}
