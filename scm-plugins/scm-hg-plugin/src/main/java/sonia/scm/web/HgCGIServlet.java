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

package sonia.scm.web;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgEnvironmentBuilder;
import sonia.scm.repository.HgPythonScript;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryRequestListenerUtil;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.util.AssertUtil;
import sonia.scm.web.cgi.CGIExecutor;
import sonia.scm.web.cgi.CGIExecutorFactory;
import sonia.scm.web.cgi.EnvList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class HgCGIServlet extends HttpServlet implements ScmProviderHttpServlet {

  /** Field description */
  private static final long serialVersionUID = -3492811300905099810L;

  /** the logger for HgCGIServlet */
  private static final Logger logger =
    LoggerFactory.getLogger(HgCGIServlet.class);

  //~--- constructors ---------------------------------------------------------

  @Inject
  public HgCGIServlet(CGIExecutorFactory cgiExecutorFactory,
                      ScmConfiguration configuration,
                      HgRepositoryHandler handler,
                      RepositoryRequestListenerUtil requestListenerUtil,
                      HgEnvironmentBuilder environmentBuilder)
  {
    this.cgiExecutorFactory = cgiExecutorFactory;
    this.configuration = configuration;
    this.handler = handler;
    this.requestListenerUtil = requestListenerUtil;
    this.environmentBuilder = environmentBuilder;
    this.exceptionHandler = new HgCGIExceptionHandler();
    this.command = HgPythonScript.HGWEB.getFile(SCMContext.getContext());
  }

  //~--- methods --------------------------------------------------------------

  @Override
  public void service(HttpServletRequest request,
    HttpServletResponse response, Repository repository)
  {
    if (!handler.isConfigured())
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
      catch (ServletException | IOException ex)
      {
        exceptionHandler.handleException(request, response, ex);
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
  private void handleRequest(HttpServletRequest request,
    HttpServletResponse response, Repository repository)
    throws ServletException, IOException
  {
    if (requestListenerUtil.callListeners(request, response, repository))
    {
      Stopwatch sw = Stopwatch.createStarted();
      process(request, response, repository);
      logger.debug("mercurial request finished in {}", sw.stop());
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
    CGIExecutor executor = cgiExecutorFactory.createExecutor(configuration,
                             getServletContext(), request, response);

    // issue #155
    executor.setPassShellEnvironment(true);
    executor.setExceptionHandler(exceptionHandler);
    executor.setStatusCodeHandler(exceptionHandler);
    executor.setContentLengthWorkaround(true);

    EnvList env = executor.getEnvironment();
    environmentBuilder.write(repository).forEach(env::set);

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
  private final CGIExecutorFactory cgiExecutorFactory;

  /** Field description */
  private final File command;

  /** Field description */
  private final ScmConfiguration configuration;

  /** Field description */
  private final HgCGIExceptionHandler exceptionHandler;

  /** Field description */
  private final HgRepositoryHandler handler;

  /** Field description */
  private final RepositoryRequestListenerUtil requestListenerUtil;

  private final HgEnvironmentBuilder environmentBuilder;
}
