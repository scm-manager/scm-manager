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



package sonia.scm.template;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.SCMContextProvider;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.Writer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class TemplateServlet extends HttpServlet
{

  /** Field description */
  private static final long serialVersionUID = 3578555653924091546L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param context
   * @param templateHandler
   */
  @Inject
  public TemplateServlet(SCMContextProvider context,
                         TemplateHandler templateHandler)
  {
    this.templateHandler = templateHandler;
    this.version = context.getVersion();
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
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    Map<String, Object> params = new HashMap<String, Object>();
    String contextPath = request.getContextPath();

    params.put("contextPath", contextPath);
    params.put("version", version);

    Locale l = request.getLocale();

    if (l == null)
    {
      l = Locale.ENGLISH;
    }

    String locale = l.toString();

    params.put("locale", locale);

    String country = locale;
    int i = country.indexOf("_");

    if (i > 0)
    {
      country = country.substring(0, i);
    }

    params.put("country", country);

    String templateName = getTemplateName(contextPath, request.getRequestURI());
    Writer writer = null;

    try
    {
      writer = response.getWriter();
      templateHandler.render(templateName, writer, params);
    }
    finally
    {
      IOUtil.close(writer);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param contextPath
   * @param requestURI
   *
   * @return
   */
  private String getTemplateName(String contextPath, String requestURI)
  {
    String path = requestURI.substring(contextPath.length());

    if (path.endsWith("/"))
    {
      path = path.concat("index.html");
    }

    return path;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private TemplateHandler templateHandler;

  /** Field description */
  private String version;
}
