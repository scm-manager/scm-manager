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
