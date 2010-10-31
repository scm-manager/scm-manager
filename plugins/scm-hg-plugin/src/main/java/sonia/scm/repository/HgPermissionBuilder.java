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

import sonia.scm.group.Group;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

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

      if (!firstRead)
      {
        read.append(", ");
      }
      else
      {
        firstRead = false;
      }

      read.append(name);

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

    Group group =    /* SCMContext.getContext().getGroupManager().get(name); */
      null;

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
