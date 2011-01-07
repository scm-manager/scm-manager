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

import com.google.inject.Provider;

import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class PermissionUtil
{

  /**
   * Method description
   *
   *
   * @param repository
   * @param securityContext
   * @param pt
   */
  public static void assertPermission(Repository repository,
          WebSecurityContext securityContext, PermissionType pt)
  {
    if (!hasPermission(repository, securityContext, pt))
    {
      throw new IllegalStateException("action denied");
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param securityContextProvider
   * @param pt
   */
  public static void assertPermission(Repository repository,
          Provider<WebSecurityContext> securityContextProvider,
          PermissionType pt)
  {
    assertPermission(repository, securityContextProvider.get(), pt);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param securityContextProvider
   * @param pt
   *
   * @return
   */
  public static boolean hasPermission(Repository repository,
          Provider<WebSecurityContext> securityContextProvider,
          PermissionType pt)
  {
    return hasPermission(repository, securityContextProvider.get(), pt);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param securityContext
   * @param pt
   *
   * @return
   */
  public static boolean hasPermission(Repository repository,
          WebSecurityContext securityContext, PermissionType pt)
  {
    User user = securityContext.getUser();
    String username = user.getName();

    AssertUtil.assertIsNotEmpty(username);

    boolean result = false;

    if (user.isAdmin()
        || ((pt == PermissionType.READ) && repository.isPublicReadable()))
    {
      result = true;
    }
    else
    {
      List<Permission> permissions = repository.getPermissions();

      if (permissions != null)
      {
        result = hasPermission(permissions, username,
                               securityContext.getGroups(), pt);
      }
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param permissions
   * @param username
   * @param groups
   * @param pt
   *
   * @return
   */
  private static boolean hasPermission(List<Permission> permissions,
          String username, Collection<String> groups, PermissionType pt)
  {
    boolean result = false;

    for (Permission p : permissions)
    {
      String name = p.getName();

      if ((name != null) && (p.getType().getValue() >= pt.getValue()))
      {
        if (name.equals(username)
            || (p.isGroupPermission() && groups.contains(p.getName())))
        {
          result = true;

          break;
        }
      }
    }

    return result;
  }
}
