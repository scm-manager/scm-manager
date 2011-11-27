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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.10
 */
@Singleton
public class RepositoryRequestListenerUtil
{

  /** the logger for RepositoryRequestListenerUtil */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryRequestListenerUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param listenerSet
   */
  @Inject
  public RepositoryRequestListenerUtil(
          Set<RepositoryRequestListener> listenerSet)
  {
    this.listenerSet = listenerSet;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param repository
   *
   * @return
   */
  public boolean callListeners(HttpServletRequest request,
                               HttpServletResponse response,
                               Repository repository)
  {
    boolean process = true;

    if (Util.isNotEmpty(listenerSet))
    {
      try
      {
        for (RepositoryRequestListener listener : listenerSet)
        {
          if (logger.isTraceEnabled())
          {
            logger.trace("call repository request listener {}",
                         listener.getClass());
          }

          if (!listener.handleRequest(request, response, repository))
          {
            process = false;

            if (logger.isDebugEnabled())
            {
              logger.debug("repository request listener {} aborted request",
                           listener.getClass());
            }
          }
        }
      }
      catch (Exception ex)
      {
        process = false;
        logger.error("repository request listener failed", ex);
      }
    }
    else if (logger.isTraceEnabled())
    {
      logger.trace("no repository request listener defined");
    }

    return process;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<RepositoryRequestListener> listenerSet;
}
