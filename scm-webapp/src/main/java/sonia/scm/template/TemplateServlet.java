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
    
package sonia.scm.template;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class TemplateServlet extends HttpServlet
{

  /** Field description */
  public static final String CONTENT_TYPE = "text/html";

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /** Field description */
  private static final long serialVersionUID = 3578555653924091546L;

  /**
   * the logger for TemplateServlet
   */
  private static final Logger logger =
    LoggerFactory.getLogger(TemplateServlet.class);

  /** Field description */
  private static final Set<Locale> DEFAULT_LOCALE =
    ImmutableSet.of(Locale.ENGLISH, Locale.UK, Locale.US);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param context
   * @param templateEngineFactory
   * @param configuration
   */
  @Inject
  public TemplateServlet(SCMContextProvider context,
    TemplateEngineFactory templateEngineFactory, ScmConfiguration configuration)
  {
    this.templateEngineFactory = templateEngineFactory;
    this.configuration = configuration;
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
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
  {
    Map<String, Object> params = new HashMap<>();
    String contextPath = request.getContextPath();

    params.put("contextPath", contextPath);
    params.put("configuration", configuration);
    params.put("version", version);

    Locale l = request.getLocale();

    if (l == null)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("could not find locale in request, use englich");
      }

      l = Locale.ENGLISH;
    }
    else if (logger.isTraceEnabled())
    {
      logger.trace("found locale {} in request", l);
    }

    String locale = l.toString();

    params.put("locale", locale);

    String country = locale;
    int i = country.indexOf('_');

    if (i > 0)
    {
      country = country.substring(0, i);
    }

    params.put("country", country);

    if (!DEFAULT_LOCALE.contains(l))
    {
      params.put("nonDefaultLocale", Boolean.TRUE);
    }

    String templateName = getTemplateName(contextPath, request.getRequestURI());
    Writer writer = null;

    try
    {
      response.setCharacterEncoding(ENCODING);
      response.setContentType(CONTENT_TYPE);
      writer = response.getWriter();

      TemplateEngine engine = templateEngineFactory.getDefaultEngine();
      Template template = engine.getTemplate(templateName);

      if (template != null)
      {
        template.execute(writer, params);
      }
      else
      {
        if (logger.isWarnEnabled())
        {
          logger.warn("could not find template {}", templateName);
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
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
      path = path.concat("index.mustache");
    }
    else
    {
      int index = path.lastIndexOf('.');

      if (index > 0)
      {
        path = path.substring(0, index).concat(".mustache");
      }
    }

    return path;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ScmConfiguration configuration;

  /** Field description */
  private final TemplateEngineFactory templateEngineFactory;

  /** Field description */
  private final String version;
}
