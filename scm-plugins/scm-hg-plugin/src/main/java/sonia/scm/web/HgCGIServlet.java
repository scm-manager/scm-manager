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
import com.google.inject.Provider;
import com.google.inject.Singleton;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.util.AssertUtil;
import sonia.scm.web.cgi.CGIExecutor;
import sonia.scm.web.cgi.CGIExecutorFactory;
import sonia.scm.web.cgi.EnvList;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Enumeration;
import java.util.regex.Pattern;

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
  public static final String ENV_PYTHON_PATH = "SCM_PYTHON_PATH";

  /** Field description */
  public static final String ENV_REPOSITORY_NAME = "REPO_NAME";

  /** Field description */
  public static final String ENV_REPOSITORY_PATH = "SCM_REPOSITORY_PATH";

  /** Field description */
  public static final String ENV_SESSION_PREFIX = "SCM_";

  /** Field description */
  private static final long serialVersionUID = -3492811300905099810L;

  /** Field description */
  public static final Pattern PATTERN_REPOSITORYNAME =
    Pattern.compile("/[^/]+/([^/]+)(?:/.*)?");

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
   * @param repositoryManager
   * @param repositoryProvider
   * @param handler
   * @param hookManager
   */
  @Inject
  public HgCGIServlet(CGIExecutorFactory cgiExecutorFactory,
                      ScmConfiguration configuration,
                      RepositoryManager repositoryManager,
                      Provider<Repository> repositoryProvider,
                      HgRepositoryHandler handler, HgHookManager hookManager)
  {
    this.cgiExecutorFactory = cgiExecutorFactory;
    this.configuration = configuration;
    this.repositoryManager = repositoryManager;
    this.repositoryProvider = repositoryProvider;
    this.handler = handler;
    this.hookManager = hookManager;
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
    command = HgUtil.getCGI();
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
      throw new ServletException("repository not found");
    }

    String name = repository.getName();
    File directory = handler.getDirectory(repository);
    String pythonPath = "";
    HgConfig config = handler.getConfig();

    if (config != null)
    {
      pythonPath = config.getPythonPath();

      if (pythonPath == null)
      {
        pythonPath = "";
      }
    }

    hookManager.writeHookScript(request);

    CGIExecutor executor = cgiExecutorFactory.createExecutor(configuration,
                             getServletContext(), request, response);

    executor.getEnvironment().set(ENV_REPOSITORY_NAME, name);
    executor.getEnvironment().set(ENV_REPOSITORY_PATH,
                                  directory.getAbsolutePath());
    executor.getEnvironment().set(ENV_PYTHON_PATH, pythonPath);

    HttpSession session = request.getSession();

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

  /**
   * Method description
   *
   *
   * @param env
   * @param session
   */
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

  /**
   * Method description
   *
   *
   * @param repositoryname
   *
   * @return
   */
  private Repository getRepository(String repositoryname)
  {
    return repositoryManager.get(HgRepositoryHandler.TYPE_NAME, repositoryname);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private CGIExecutorFactory cgiExecutorFactory;

  /** Field description */
  private File command;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private HgHookManager hookManager;

  /** Field description */
  private RepositoryManager repositoryManager;

  /** Field description */
  private Provider<Repository> repositoryProvider;
}
