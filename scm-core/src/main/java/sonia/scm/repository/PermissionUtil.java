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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.RepositoryPermission;
import sonia.scm.security.Role;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.util.AssertUtil;
import sonia.scm.web.security.WebSecurityContext;

/**
 *
 * @author Sebastian Sdorra
 */
public final class PermissionUtil
{

  /**
   * the logger for PermissionUtil
   */
  private static final Logger logger =
    LoggerFactory.getLogger(PermissionUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private PermissionUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param securityContext
   * @param pt
   * @deprecated
   */
  @Deprecated
  public static void assertPermission(Repository repository,
    WebSecurityContext securityContext, PermissionType pt)
  {
    assertPermission(repository, pt);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param securityContextProvider
   * @param pt
   */
  @Deprecated
  public static void assertPermission(Repository repository,
    Provider<WebSecurityContext> securityContextProvider, PermissionType pt)
  {
    assertPermission(repository, securityContextProvider.get(), pt);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param pt
   *
   * @since 1.21
   */
  @Deprecated
  public static void assertPermission(Repository repository, PermissionType pt)
  {
    if (!hasPermission(null, repository, pt))
    {
      throw new ScmSecurityException("action denied");
    }
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
   * @deprecated
   */
  @Deprecated
  public static boolean hasPermission(Repository repository,
    Provider<WebSecurityContext> securityContextProvider, PermissionType pt)
  {
    return hasPermission(null, repository, pt);
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
   * @deprecated use {@link #hasPermission(ScmConfiguration, Repository, PermissionType)} instead
   */
  @Deprecated
  public static boolean hasPermission(Repository repository,
    WebSecurityContext securityContext, PermissionType pt)
  {
    return hasPermission(null, repository, pt);
  }

  /**
   * Method description
   *
   *
   * @param configuration
   * @param repository
   * @param pt
   *
   * @return
   *
   * @since 1.21
   */
  public static boolean hasPermission(ScmConfiguration configuration,
    Repository repository, PermissionType pt)
  {
    boolean result;

    Subject subject = SecurityUtils.getSubject();

    if (subject.isAuthenticated() || subject.isRemembered())
    {
      String username = subject.getPrincipal().toString();

      AssertUtil.assertIsNotEmpty(username);

      if (subject.hasRole(Role.ADMIN)
        || ((pt == PermissionType.READ) && repository.isPublicReadable()))
      {
        result = true;
      }
      else
      {
        result = subject.isPermitted(new RepositoryPermission(repository, pt));
      }
    }
    else
    {

      // check anonymous access
      result = (configuration != null)
        && configuration.isAnonymousAccessEnabled()
        && repository.isPublicReadable() && (pt == PermissionType.READ);
    }

    return result;
  }

  /**
   * Returns true if the repository is writable.
   *
   *
   * @param configuration SCM-Manager main configuration
   * @param repository repository to check
   * @param securityContext current user security context
   *
   * @return true if the repository is writable
   * @since 1.14
   * @deprecated use {@link #isWritable(ScmConfiguration, Repository)} instead
   */
  @Deprecated
  public static boolean isWritable(ScmConfiguration configuration,
    Repository repository, WebSecurityContext securityContext)
  {
    return isWritable(configuration, repository);
  }

  /**
   * Returns true if the repository is writable.
   *
   *
   * @param configuration SCM-Manager main configuration
   * @param repository repository to check
   *
   * @return true if the repository is writable
   * @since 1.21
   */
  public static boolean isWritable(ScmConfiguration configuration,
    Repository repository)
  {
    boolean permitted = false;

    if (configuration.isEnableRepositoryArchive() && repository.isArchived())
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("{} is archived and is not writeable",
          repository.getName());
      }
    }
    else
    {
      permitted = PermissionUtil.hasPermission(configuration, repository,
        PermissionType.WRITE);
    }

    return permitted;
  }
}
