/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
