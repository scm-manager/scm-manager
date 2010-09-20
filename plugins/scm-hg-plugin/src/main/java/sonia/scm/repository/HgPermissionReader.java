/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgPermissionReader
{

  /**
   * Method description
   *
   *
   * @param read
   * @param write
   *
   * @return
   */
  public List<Permission> readPermissions(String read, String write)
  {
    List<String> readPerms = createPermList(read);
    List<String> writePerms = createPermList(write);

    return createPermissionList(readPerms, writePerms);
  }

  /**
   * Method description
   *
   *
   * @param perms
   *
   * @return
   */
  private List<String> createGroups(List<String> perms)
  {
    List<String> groups = new ArrayList<String>();
    boolean start = false;
    Iterator<String> permIt = perms.iterator();

    while (permIt.hasNext())
    {
      String perm = permIt.next();

      if (perm.startsWith("__"))
      {
        if (start)
        {
          start = false;
        }
        else
        {
          start = true;
          groups.add(perm.substring(2));
        }

        permIt.remove();
      }
      else if (start)
      {
        permIt.remove();
      }
    }

    return groups;
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  private List<String> createPermList(String value)
  {
    List<String> perms = new ArrayList<String>();

    if (Util.isNotEmpty(value))
    {
      String[] values = value.split(",");

      for (String name : values)
      {
        perms.add(name.trim());
      }
    }

    return perms;
  }

  /**
   * Method description
   *
   *
   * @param readPerms
   * @param writePerms
   *
   * @return
   */
  private List<Permission> createPermissionList(List<String> readPerms,
          List<String> writePerms)
  {
    List<Permission> permissions = new ArrayList<Permission>();
    List<String> readGroups = createGroups(readPerms);
    List<String> writeGroups = createGroups(writePerms);

    createPermissions(permissions, readPerms, writePerms, false);
    createPermissions(permissions, readGroups, writeGroups, true);

    return permissions;
  }

  /**
   * Method description
   *
   *
   * @param permissions
   * @param readPermissions
   * @param writePermissions
   * @param group
   */
  private void createPermissions(List<Permission> permissions,
                                 List<String> readPermissions,
                                 List<String> writePermissions, boolean group)
  {
    for (String readPerm : readPermissions)
    {
      boolean write = false;

      if (writePermissions.contains(readPerm))
      {
        write = true;
        writePermissions.remove(readPerm);
      }

      permissions.add(new Permission(readPerm, write, group));
    }

    for (String writePerm : writePermissions)
    {
      permissions.add(new Permission(writePerm, true, group));
    }
  }
}
