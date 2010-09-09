/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sonia.scm.repository;

import java.util.Collection;
import sonia.scm.group.Group;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgPermissionBuilder
{

  /**
   * Constructs ...
   *
   *
   * @param permissions
   */
  public HgPermissionBuilder(Collection<Permission> permissions)
  {
    buildPermissions(permissions);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getReadPermission()
  {
    return read.toString();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getWritePermission()
  {
    return write.toString();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param permissions
   */
  private void buildPermissions(Collection<Permission> permissions)
  {
    boolean firstRead = true;
    boolean firstWrite = true;

    for (Permission permission : permissions)
    {
      String name = permission.getName();

      if (permission.isGroupPermission())
      {
        name = createGroupString(name);
      }

      if (permission.isReadable())
      {
        if (!firstRead)
        {
          read.append(", ");
        }
        else
        {
          firstRead = false;
        }

        read.append(name);
      }

      if (permission.isWriteable())
      {
        if (!firstWrite)
        {
          write.append(", ");
        }
        else
        {
          firstWrite = false;
        }

        write.append(name);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  private String createGroupString(String name)
  {
    StringBuilder result = new StringBuilder();

    result.append("__").append(name).append(", ");

    Group group = /*SCMContext.getContext().getGroupManager().get(name);*/ null;

    if (group != null)
    {
      Collection<String> memerbers = group.getMembers();

      if (memerbers != null)
      {
        for (String member : memerbers)
        {
          result.append(member).append(", ");
        }
      }
    }

    result.append("__").append(name);

    return result.toString();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private StringBuilder read = new StringBuilder();

  /** Field description */
  private StringBuilder write = new StringBuilder();
}
