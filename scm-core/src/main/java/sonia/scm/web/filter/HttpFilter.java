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

package sonia.scm.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


public abstract class HttpFilter implements Filter
{


  protected abstract void doFilter(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain chain)
          throws IOException, ServletException;

   @Override
  public void destroy()
  {

    // do nothing
  }


  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
                       FilterChain chain)
          throws IOException, ServletException
  {
    if ((request instanceof HttpServletRequest)
        && (response instanceof HttpServletResponse))
    {
      doFilter((HttpServletRequest) request, (HttpServletResponse) response,
               chain);
    }
    else
    {
      throw new IllegalArgumentException("request is not an http request");
    }
  }

  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException
  {

    // do nothing
  }
}
