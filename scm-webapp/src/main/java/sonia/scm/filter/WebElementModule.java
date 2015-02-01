/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.WebElementDescriptor;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author Sebastian Sdorra
 */
public class WebElementModule extends ServletModule
{

  /**
   * the logger for WebElementModule
   */
  private static final Logger logger =
    LoggerFactory.getLogger(WebElementModule.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param pluginLoader
   */
  public WebElementModule(PluginLoader pluginLoader)
  {
    collector = WebElementCollector.collect(pluginLoader);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void configureServlets()
  {
    for (TypedWebElementDescriptor<? extends Filter> f : collector.getFilters())
    {
      bindFilter(f);
    }

    for (TypedWebElementDescriptor<? extends HttpServlet> s :
      collector.getServlets())
    {
      bindServlet(s);
    }
  }

  /**
   * Method description
   *
   *
   * @param filter
   */
  private void bindFilter(TypedWebElementDescriptor<? extends Filter> filter)
  {
    Class<? extends Filter> clazz = filter.getClazz();

    logger.info("bind filter {} to filter chain", clazz);

    // filters must be in singleton scope
    bind(clazz).in(Scopes.SINGLETON);

    WebElementDescriptor opts = filter.getDescriptor();
    FilterKeyBindingBuilder builder;

    if (opts.isRegex())
    {
      builder = filterRegex(opts.getPattern(), opts.getMorePatterns());
    }
    else
    {
      builder = filter(opts.getPattern(), opts.getMorePatterns());
    }

    // TODO handle init parameters
    builder.through(clazz);
  }

  /**
   * Method description
   *
   *
   * @param servlet
   */
  private void bindServlet(
    TypedWebElementDescriptor<? extends HttpServlet> servlet)
  {
    Class<? extends HttpServlet> clazz = servlet.getClazz();

    logger.info("bind servlet {} to servlet chain", clazz);

    // filters must be in singleton scope
    bind(clazz).in(Scopes.SINGLETON);

    WebElementDescriptor opts = servlet.getDescriptor();
    ServletKeyBindingBuilder builder;

    if (opts.isRegex())
    {
      builder = serveRegex(opts.getPattern(), opts.getMorePatterns());
    }
    else
    {
      builder = serve(opts.getPattern(), opts.getMorePatterns());
    }

    // TODO handle init parameters
    builder.with(clazz);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final WebElementCollector collector;
}
