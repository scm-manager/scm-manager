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
