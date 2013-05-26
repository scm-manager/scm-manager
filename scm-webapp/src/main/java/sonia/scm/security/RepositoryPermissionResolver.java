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



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Splitter;

import org.apache.shiro.authz.permission.PermissionResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.PermissionType;

//~--- JDK imports ------------------------------------------------------------

import java.util.Iterator;
import java.util.Locale;

/**
 *
 * @author Sebastian Sdorra
 */
public class RepositoryPermissionResolver implements PermissionResolver
{

  /**
   * the logger for RepositoryPermissionResolver
   */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryPermissionResolver.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param permissionString
   *
   * @return
   */
  @Override
  public RepositoryPermission resolvePermission(String permissionString)
  {
    RepositoryPermission permission = null;
    Iterator<String> permissionIt =
      Splitter.on(':').omitEmptyStrings().trimResults().split(
        permissionString).iterator();

    if (permissionIt.hasNext())
    {
      String type = permissionIt.next();

      if (type.equals(RepositoryPermission.TYPE))
      {
        permission = createRepositoryPermission(permissionIt);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("permission '{}' is not a repository permission",
          permissionString);
      }
    }

    return permission;
  }

  /**
   * Method description
   *
   *
   * @param permissionIt
   *
   * @return
   */
  private RepositoryPermission createRepositoryPermission(
    Iterator<String> permissionIt)
  {
    RepositoryPermission permission = null;

    if (permissionIt.hasNext())
    {
      String repositoryId = permissionIt.next();

      if (permissionIt.hasNext())
      {
        try
        {
          String typeString = permissionIt.next();

          typeString = typeString.trim().toUpperCase(Locale.ENGLISH);

          PermissionType type = PermissionType.valueOf(typeString);

          permission = new RepositoryPermission(repositoryId, type);
        }
        catch (IllegalArgumentException ex)
        {
          logger.warn("type is not a valid permission type", ex);
        }
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("permission type is missing");
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("repository id and permission type is missing");
    }

    return permission;
  }
}
