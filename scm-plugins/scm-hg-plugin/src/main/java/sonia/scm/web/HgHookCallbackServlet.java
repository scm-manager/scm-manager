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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.HgRepositoryHookEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class HgHookCallbackServlet extends HttpServlet
{

  /** Field description */
  private static final String PARAM_CHALLENGE = "challenge";

  /** Field description */
  private static final String PARAM_NODE = "node";

  /** Field description */
  private static final Pattern REGEX_URL =
    Pattern.compile("^/hook/hg/([^/]+)/([^/]+)$");

  /** the logger for HgHookCallbackServlet */
  private static final Logger logger =
    LoggerFactory.getLogger(HgHookCallbackServlet.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   * @param handler
   * @param hookManager
   */
  @Inject
  public HgHookCallbackServlet(RepositoryManager repositoryManager,
                               HgRepositoryHandler handler,
                               HgHookManager hookManager)
  {
    this.repositoryManager = repositoryManager;
    this.handler = handler;
    this.hookManager = hookManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doPost(HttpServletRequest request,
                        HttpServletResponse response)
          throws ServletException, IOException
  {
    String strippedURI = HttpUtil.getStrippedURI(request);
    Matcher m = REGEX_URL.matcher(strippedURI);

    if (m.matches())
    {
      String repositoryId = m.group(1);
      String type = m.group(2);
      String challenge = request.getParameter(PARAM_CHALLENGE);

      if (Util.isNotEmpty(challenge))
      {
        String node = request.getParameter(PARAM_NODE);

        if (Util.isNotEmpty(node))
        {
          hookCallback(response, repositoryId, type, challenge, node);
        }
        else if (logger.isDebugEnabled())
        {
          logger.debug("node parameter not found");
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("challenge parameter not found");
      }
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("url does not match");
      }

      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   * @param repositoryName
   * @param type
   * @param challenge
   * @param node
   *
   * @throws IOException
   */
  private void hookCallback(HttpServletResponse response,
                            String repositoryName, String type,
                            String challenge, String node)
          throws IOException
  {
    if (hookManager.isAcceptAble(challenge))
    {
      try
      {
        repositoryManager.fireHookEvent(HgRepositoryHandler.TYPE_NAME,
                                        repositoryName,
                                        new HgRepositoryHookEvent(handler,
                                          repositoryName, node));
      }
      catch (RepositoryNotFoundException ex)
      {
        if (logger.isErrorEnabled())
        {
          logger.error("could not find repository {}", repositoryName);

          if (logger.isTraceEnabled())
          {
            logger.trace("repository not found", ex);
          }
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    }
    else
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("hg hook challenge is not accept able");
      }

      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private HgHookManager hookManager;

  /** Field description */
  private RepositoryManager repositoryManager;
}
