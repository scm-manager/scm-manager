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



package sonia.scm.web.cgi;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractCGIServlet extends HttpServlet
{

  /** Field description */
  private static final long serialVersionUID = -8638099037069714140L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   */
  public AbstractCGIServlet(ScmConfiguration configuration)
  {
    this.configuration = configuration;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param req
   *
   * @return
   *
   * @throws IOException
   * @throws ServletException
   */
  protected abstract File getCommand(HttpServletRequest req)
          throws ServletException, IOException;

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
    cgiRunner = new CGIRunner(getServletContext(), null, isExitStateIgnored());
    baseEnvironment = createBaseEnvironment();
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws ServletException
   */
  protected EnvList createBaseEnvironment() throws ServletException
  {
    EnvList env = new EnvList();
    Enumeration e = getInitParameterNames();

    while (e.hasMoreElements())
    {
      String n = (String) e.nextElement();

      if ((n != null) && n.startsWith("ENV_"))
      {
        env.set(n.substring(4), getInitParameter(n));
      }
    }

    if (!env.containsKey("SystemRoot"))
    {
      String os = System.getProperty("os.name");

      if ((os != null) && (os.toLowerCase().indexOf("windows") != -1))
      {
        env.set("SystemRoot", "C:\\WINDOWS");
      }
    }

    return env;
  }

  /**
   * Method description
   *
   *
   *
   * @param request
   * @param baseEnvironment
   *
   * @return
   *
   * @throws ServletException
   */
  protected EnvList createRequestEnvironment(HttpServletRequest request,
          EnvList baseEnvironment)
          throws ServletException
  {
    return new EnvList(baseEnvironment);
  }

  /**
   * Method description
   *
   *
   * @param req
   * @param resp
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException
  {
    cgiRunner.exec(createRequestEnvironment(req, baseEnvironment),
                   getCmdPrefix(), getCommand(req), req.getPathInfo(), req,
                   resp, HttpUtil.getServerPort(configuration, req));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected String getCmdPrefix()
  {
    return null;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  protected boolean isExitStateIgnored()
  {
    return false;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private EnvList baseEnvironment;

  /** Field description */
  private CGIRunner cgiRunner;

  /** Field description */
  private ScmConfiguration configuration;
}
