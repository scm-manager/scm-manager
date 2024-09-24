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

package sonia.scm.filter;


import com.google.inject.Singleton;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.WebUtil;
import sonia.scm.web.filter.HttpFilter;

import java.io.File;
import java.io.IOException;


@Singleton
public class StaticResourceFilter extends HttpFilter
{

  private static final Logger logger =
    LoggerFactory.getLogger(StaticResourceFilter.class);

  private ServletContext context;
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException
  {
    this.context = filterConfig.getServletContext();
  }


  @Override
  protected void doFilter(HttpServletRequest request,
                          HttpServletResponse response, FilterChain chain)
          throws IOException, ServletException
  {
    String uri = request.getRequestURI();
    File resource = getResourceFile(request, uri);

    if (!resource.exists())
    {
      WebUtil.addETagHeader(response, resource);
      WebUtil.addStaticCacheControls(response, WebUtil.TIME_YEAR);

      if (!WebUtil.isModified(request, resource))
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("return {} for {}" , HttpServletResponse.SC_NOT_MODIFIED, uri);
        }

        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      }
      else
      {
        chain.doFilter(request, response);
      }
    }
    else
    {
      chain.doFilter(request, response);
    }
  }



  private File getResourceFile(HttpServletRequest request, String uri)
  {
    String path = uri.substring(request.getContextPath().length());

    return new File(context.getRealPath(path));
  }

}
