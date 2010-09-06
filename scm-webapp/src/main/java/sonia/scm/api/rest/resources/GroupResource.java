/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Group;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.LinkedHashMap;

import javax.inject.Singleton;

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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("groups")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class GroupResource
{

  /**
   * Constructs ...
   *
   */
  public GroupResource()
  {
    groupStore = new LinkedHashMap<String, Group>();
    groupStore.put("csit",
                   new Group("csit", "th", "merlec", "hopper", "oelkersd",
                             "sdorra", "gollnict"));
    groupStore.put("devel",
                   new Group("devel", "sdorra", "th", "merlec", "oelkersd"));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param group
   *
   * @return
   */
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response add(Group group)
  {
    groupStore.put(group.getName(), group);

    return Response.created(
        uriInfo.getAbsolutePath().resolve(
          "groups/".concat(group.getName()))).build();
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  @DELETE
  @Path("{name}")
  public Response delete(@PathParam("name") String name)
  {
    Group group = groupStore.get(name);

    if (group == null)
    {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    groupStore.remove(name);

    return Response.noContent().build();
  }

  /**
   * Method description
   *
   *
   *
   * @param name
   * @param group
   *
   * @return
   */
  @PUT
  @Path("{name}")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response update(@PathParam("name") String name, Group group)
  {
    Group updateGroup = groupStore.get(name);

    if (updateGroup == null)
    {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    updateGroup.setName(name);
    updateGroup.setMembers(group.getMembers());

    return Response.created(
        uriInfo.getAbsolutePath().resolve(group.getName())).build();
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
  public Group get(@PathParam("name") String name)
  {
    Group group = groupStore.get(name);

    if (group == null)
    {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    return group;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  public Group[] getAll()
  {
    Collection<Group> groupCollection = groupStore.values();

    return groupCollection.toArray(new Group[groupCollection.size()]);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private LinkedHashMap<String, Group> groupStore;

  /** Field description */
  @Context
  private UriInfo uriInfo;
}
