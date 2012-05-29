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



package sonia.scm.web.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Throwables;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.9
 */
public abstract class ProviderPermissionFilter extends PermissionFilter
{

  /**
   * the logger for ProviderPermissionFilter
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ProviderPermissionFilter.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param configuration
   * @param securityContextProvider
   * @param repositoryProvider
   */
  public ProviderPermissionFilter(
          ScmConfiguration configuration,
          Provider<WebSecurityContext> securityContextProvider,
          RepositoryProvider repositoryProvider)
  {
    super(configuration, securityContextProvider);
    this.repositoryProvider = repositoryProvider;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @Override
  protected Repository getRepository(HttpServletRequest request)
  {
    Repository repository = null;

    try
    {
      repository = repositoryProvider.get();
    }
    catch (ProvisionException ex)
    {
      Throwables.propagateIfInstanceOf(ex.getCause(),
                                       IllegalStateException.class);

      if (logger.isErrorEnabled())
      {
        logger.error("could not get repository from request", ex);
      }
    }

    return repository;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private RepositoryProvider repositoryProvider;
}
