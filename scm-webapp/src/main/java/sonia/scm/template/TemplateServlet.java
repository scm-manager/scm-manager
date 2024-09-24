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

package sonia.scm.template;


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


@Singleton
public class TemplateServlet extends HttpServlet
{

  public static final String CONTENT_TYPE = "text/html";

  public static final String ENCODING = "UTF-8";

  private static final long serialVersionUID = 3578555653924091546L;

 
  private static final Logger logger =
    LoggerFactory.getLogger(TemplateServlet.class);

  private static final Set<Locale> DEFAULT_LOCALE =
    ImmutableSet.of(Locale.ENGLISH, Locale.UK, Locale.US);

  private final ScmConfiguration configuration;

  private final TemplateEngineFactory templateEngineFactory;

  private final String version;

  @Inject
  public TemplateServlet(SCMContextProvider context,
    TemplateEngineFactory templateEngineFactory, ScmConfiguration configuration)
  {
    this.templateEngineFactory = templateEngineFactory;
    this.configuration = configuration;
    this.version = context.getVersion();
  }



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

}
