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


import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.WebElementDescriptor;


public class WebElementModule extends ServletModule {

  private static final Logger LOG = LoggerFactory.getLogger(WebElementModule.class);

  private final WebElementCollector collector;

  public WebElementModule(PluginLoader pluginLoader)
  {
    collector = WebElementCollector.collect(pluginLoader);
  }

  @Override
  protected void configureServlets()
  {
    for (TypedWebElementDescriptor<Filter> f : collector.getFilters())
    {
      bindFilter(f);
    }

    for (TypedWebElementDescriptor<HttpServlet> s : collector.getServlets())
    {
      bindServlet(s);
    }
  }

  private void bindFilter(TypedWebElementDescriptor<Filter> filter) {
    Class<Filter> clazz = filter.getClazz();

    LOG.info("bind filter {} to filter chain", clazz);

    // filters must be in singleton scope
    bind(clazz).in(Scopes.SINGLETON);

    WebElementDescriptor opts = filter.getDescriptor();
    FilterKeyBindingBuilder builder;

    if (opts.isRegex()) {
      LOG.debug("bind regex filter {} to {} and {}", clazz, opts.getPattern(), opts.getMorePatterns());
      builder = filterRegex(opts.getPattern(), opts.getMorePatterns());
    }
    else
    {
      LOG.debug("bind glob filter {} to {} and {}", clazz, opts.getPattern(), opts.getMorePatterns());
      builder = filter(opts.getPattern(), opts.getMorePatterns());
    }

    // TODO handle init parameters
    builder.through(clazz);
  }

  private void bindServlet(TypedWebElementDescriptor<HttpServlet> servlet) {
    Class<HttpServlet> clazz = servlet.getClazz();

    LOG.info("bind servlet {} to servlet chain", clazz);

    // filters must be in singleton scope
    bind(clazz).in(Scopes.SINGLETON);

    WebElementDescriptor opts = servlet.getDescriptor();
    ServletKeyBindingBuilder builder;

    if (opts.isRegex()) {
      LOG.debug("bind regex servlet {} to {} and {}", clazz, opts.getPattern(), opts.getMorePatterns());
      builder = serveRegex(opts.getPattern(), opts.getMorePatterns());
    } else {
      LOG.debug("bind glob servlet {} to {} and {}", clazz, opts.getPattern(), opts.getMorePatterns());
      builder = serve(opts.getPattern(), opts.getMorePatterns());
    }

    // TODO handle init parameters
    builder.with(clazz);
  }

}
