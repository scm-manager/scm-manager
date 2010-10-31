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
