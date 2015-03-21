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

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgEnvironment;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgPythonScript;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.repository.RepositoryRequestListenerUtil;
import sonia.scm.security.CipherUtil;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.cgi.CGIExecutor;
import sonia.scm.web.cgi.CGIExecutorFactory;
import sonia.scm.web.cgi.EnvList;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.core.util.Base64;

import java.io.File;
import java.io.IOException;

import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class HgCGIServlet extends HttpServlet
{

  /** Field description */
  public static final String ENV_REPOSITORY_NAME = "REPO_NAME";

  /** Field description */
  public static final String ENV_REPOSITORY_PATH = "SCM_REPOSITORY_PATH";

  /** Field description */
  public static final String ENV_SESSION_PREFIX = "SCM_";

  /** Field description */
  private static final String SCM_CREDENTIALS = "SCM_CREDENTIALS";

  /** Field description */
  private static final long serialVersionUID = -3492811300905099810L;

  /** the logger for HgCGIServlet */
  private static final Logger logger =
    LoggerFactory.getLogger(HgCGIServlet.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   *
   *
   * @param cgiExecutorFactory
   * @param configuration
   * @param repositoryProvider
   * @param handler
   * @param hookManager
   * @param requestListenerUtil
   */
  @Inject
  public HgCGIServlet(CGIExecutorFactory cgiExecutorFactory,
    ScmConfiguration configuration, RepositoryProvider repositoryProvider,
    HgRepositoryHandler handler, HgHookManager hookManager,
    RepositoryRequestListenerUtil requestListenerUtil)
  {
    this.cgiExecutorFactory = cgiExecutorFactory;
    this.configuration = configuration;
    this.repositoryProvider = repositoryProvider;
    this.handler = handler;
    this.hookManager = hookManager;
    this.requestListenerUtil = requestListenerUtil;
    this.exceptionHandler = new HgCGIExceptionHandler();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws ServletException
   */
  @Override
  public void init() throws ServletException
  {
    command = HgPythonScript.HGWEB.getFile(SCMContext.getContext());
    super.init();
  }

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
  protected void service(HttpServletRequest request,
    HttpServletResponse response)
    throws ServletException, IOException
  {
    Repository repository = repositoryProvider.get();

    if (repository == null)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("no hg repository found at {}", request.getRequestURI());
      }

      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
    else if (!handler.isConfigured())
    {
      exceptionHandler.sendFormattedError(request, response,
        HgCGIExceptionHandler.ERROR_NOT_CONFIGURED);
    }
    else
    {
      try
      {
        handleRequest(request, response, repository);
      }
      catch (Exception ex)
      {
        exceptionHandler.handleException(request, response, ex);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param env
   * @param request
   */
  private void addCredentials(EnvList env, HttpServletRequest request)
  {
    String authorization = request.getHeader(HttpUtil.HEADER_AUTHORIZATION);

    if (!Strings.isNullOrEmpty(authorization))
    {
      if (authorization.startsWith(HttpUtil.AUTHORIZATION_SCHEME_BASIC))
      {
        String encodedUserInfo =
          authorization.substring(
            HttpUtil.AUTHORIZATION_SCHEME_BASIC.length()).trim();
        String userInfo = Base64.base64Decode(encodedUserInfo);

        env.set(SCM_CREDENTIALS, CipherUtil.getInstance().encode(userInfo));
      }
      else
      {
        logger.warn("unknow authentication scheme used");
      }
    }
    else
    {
      logger.trace("no authorization header found");
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param repository
   *
   * @throws IOException
   * @throws ServletException
   */
  private void handleRequest(HttpServletRequest request,
    HttpServletResponse response, Repository repository)
    throws ServletException, IOException
  {
    if (requestListenerUtil.callListeners(request, response, repository))
    {
      process(request, response, repository);
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("request aborted by repository request listener");
    }
  }

  /**
   * Method description
   *
   *
   * @param env
   * @param session
   */
  @SuppressWarnings("unchecked")
  private void passSessionAttributes(EnvList env, HttpSession session)
  {
    Enumeration<String> enm = session.getAttributeNames();

    while (enm.hasMoreElements())
    {
      String key = enm.nextElement();

      if (key.startsWith(ENV_SESSION_PREFIX))
      {
        env.set(key, session.getAttribute(key).toString());
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param repository
   *
   * @throws IOException
   * @throws ServletException
   */
  private void process(HttpServletRequest request,
    HttpServletResponse response, Repository repository)
    throws IOException, ServletException
  {
    String name = repository.getName();
    File directory = handler.getDirectory(repository);
    CGIExecutor executor = cgiExecutorFactory.createExecutor(configuration,
                             getServletContext(), request, response);

    // issue #155
    executor.setPassShellEnvironment(true);
    executor.setExceptionHandler(exceptionHandler);
    executor.setStatusCodeHandler(exceptionHandler);
    executor.setContentLengthWorkaround(true);
    executor.getEnvironment().set(ENV_REPOSITORY_NAME, name);
    executor.getEnvironment().set(ENV_REPOSITORY_PATH,
      directory.getAbsolutePath());

    // add hook environment
    //J-
    HgEnvironment.prepareEnvironment(
      executor.getEnvironment().asMutableMap(),
      handler,
      hookManager, 
      request
    );
    //J+

    addCredentials(executor.getEnvironment(), request);

    // unused ???
    HttpSession session = request.getSession(false);

    if (session != null)
    {
      passSessionAttributes(executor.getEnvironment(), session);
    }

    String interpreter = getInterpreter();

    if (interpreter != null)
    {
      executor.setInterpreter(interpreter);
    }

    executor.execute(command.getAbsolutePath());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private String getInterpreter()
  {
    HgConfig config = handler.getConfig();

    AssertUtil.assertIsNotNull(config);

    String python = config.getPythonBinary();

    if ((python != null) && config.isUseOptimizedBytecode())
    {
      python = python.concat(" -O");
    }

    return python;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private CGIExecutorFactory cgiExecutorFactory;

  /** Field description */
  private File command;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private HgCGIExceptionHandler exceptionHandler;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private HgHookManager hookManager;

  /** Field description */
  private RepositoryProvider repositoryProvider;

  /** Field description */
  private RepositoryRequestListenerUtil requestListenerUtil;
}
