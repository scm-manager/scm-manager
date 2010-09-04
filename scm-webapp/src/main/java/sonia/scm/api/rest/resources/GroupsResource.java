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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("groups")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class GroupsResource
{

  /**
   * Constructs ...
   *
   */
  public GroupsResource()
  {
    groupStore = new LinkedHashMap<String, Group>();
    groupStore.put("csit",
                   new Group("csit", "th", "merlec", "hopper", "oelkersd",
                             "sdorra", "gollnict"));
    groupStore.put("devel",
                   new Group("devel", "sdorra", "th", "merlec", "oelkersd"));
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

    System.out.println( group );

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
}
