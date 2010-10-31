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

import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.util.IOUtil;
import sonia.scm.web.cgi.AbstractCGIServlet;
import sonia.scm.web.cgi.EnvList;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class GitCGIServlet extends AbstractCGIServlet
{

  /** Field description */
  public static final String ENV_HTTP_EXPORT_ALL = "GIT_HTTP_EXPORT_ALL";

  /** Field description */
  public static final String ENV_PROJECT_ROOT = "GIT_PROJECT_ROOT";

  /** Field description */
  public static final String MIMETYPE_HTML = "text/html";

  /** Field description */
  public static final String REGEX_GITHTTPBACKEND =
    "(?x)^/git/(.*/(HEAD|info/refs|objects/(info/[^/]+|[0-9a-f]{2}/[0-9a-f]{38}|pack/pack-[0-9a-f]{40}\\.(pack|idx))|git-(upload|receive)-pack))$";

  /** Field description */
  public static final String RESOURCE_GITINDEX = "/sonia/scm/git.index.html";

  /** Field description */
  private static final long serialVersionUID = 9147517765161830847L;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws ServletException
   */
  @Override
  protected EnvList createBaseEnvironment() throws ServletException
  {
    EnvList list = super.createBaseEnvironment();

    list.set(ENV_HTTP_EXPORT_ALL, "");

    return list;
  }

  /**
   * Method description
   *
   *
   * @param baseEnvironment
   *
   * @return
   *
   * @throws ServletException
   */
  @Override
  protected EnvList createRequestEnvironment(EnvList baseEnvironment)
          throws ServletException
  {
    GitConfig config = repositoryHandler.getConfig();

    if (config == null)
    {
      throw new ServletException("git is not configured");
    }

    EnvList env = new EnvList(baseEnvironment);

    env.set(ENV_PROJECT_ROOT, config.getRepositoryDirectory().getPath());

    return env;
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
    String uri = getRelativePath(request);

    if (uri.matches(REGEX_GITHTTPBACKEND))
    {
      super.service(request, response);
    }
    else
    {
      printGitInformation(response);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected File getCommand(HttpServletRequest request)
          throws ServletException, IOException
  {
    GitConfig config = repositoryHandler.getConfig();

    if (config == null)
    {
      throw new ServletException("git is not configured");
    }

    return new File(config.getGitHttpBackend());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  private void printGitInformation(HttpServletResponse response)
          throws ServletException, IOException
  {
    response.setContentType(MIMETYPE_HTML);

    InputStream input = null;
    OutputStream output = null;

    try
    {
      input = GitCGIServlet.class.getResourceAsStream(RESOURCE_GITINDEX);
      output = response.getOutputStream();
      IOUtil.copy(input, output);
    }
    finally
    {
      IOUtil.close(input);
      IOUtil.close(output);
    }
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
  private String getRelativePath(HttpServletRequest request)
  {
    return request.getRequestURI().substring(request.getContextPath().length());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Inject
  private GitRepositoryHandler repositoryHandler;
}
