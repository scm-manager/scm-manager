/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.group.Group;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.LinkedHashMap;

import javax.inject.Singleton;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("groups")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class GroupResource extends AbstractResource<Group>
{

  /** Field description */
  public static final String PATH_PART = "groups";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public GroupResource()
  {
    groupStore = new LinkedHashMap<String, Group>();
    groupStore.put("csit",
                   new Group("static", "csit", "th", "merlec", "hopper",
                             "oelkersd", "sdorra", "gollnict"));
    groupStore.put("devel",
                   new Group("static", "devel", "sdorra", "th", "merlec",
                             "oelkersd"));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   */
  @Override
  protected void addItem(Group item)
  {
    groupStore.put(item.getName(), item);
  }

  /**
   * Method description
   *
   *
   * @param item
   */
  @Override
  protected void removeItem(Group item)
  {
    groupStore.remove(item.getName());
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param item
   */
  @Override
  protected void updateItem(String name, Group item)
  {
    Group group = groupStore.get(name);

    group.setMembers(item.getMembers());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Group[] getAllItems()
  {
    Collection<Group> groupCollection = groupStore.values();

    return groupCollection.toArray(new Group[groupCollection.size()]);
  }

  /**
   * Method description
   *
   *
   * @param item
   *
   * @return
   */
  @Override
  protected String getId(Group item)
  {
    return item.getName();
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  @Override
  protected Group getItem(String name)
  {
    return groupStore.get(name);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getPathPart()
  {
    return PATH_PART;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private LinkedHashMap<String, Group> groupStore;
}
