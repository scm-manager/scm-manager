/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.Util;

import java.util.Set;

/**
 *
 * @since 1.10
 */
@Singleton
public class RepositoryRequestListenerUtil
{

  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryRequestListenerUtil.class);

  private Set<RepositoryRequestListener> listenerSet;
 
  @Inject
  public RepositoryRequestListenerUtil(
          Set<RepositoryRequestListener> listenerSet)
  {
    this.listenerSet = listenerSet;
  }


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

}
