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


import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.Inject;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Nullable;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Default;
import sonia.scm.metrics.Metrics;
import sonia.scm.plugin.PluginLoader;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Default
public class MustacheTemplateEngine implements TemplateEngine
{

  /**
   * Used to implement optional injection for the PluginLoader.
   * @see <a href="https://github.com/google/guice/wiki/FrequentlyAskedQuestions#how-can-i-inject-optional-parameters-into-a-constructor">Optional Injection</a>
   */
  static class PluginLoaderHolder {
    @Inject(optional = true) PluginLoader pluginLoader;
  }

  /**
   * Used to implement optional injection for the MeterRegistry.
   * @see <a href="https://github.com/google/guice/wiki/FrequentlyAskedQuestions#how-can-i-inject-optional-parameters-into-a-constructor">Optional Injection</a>
   */
  static class MeterRegistryHolder {
    @Inject(optional = true) MeterRegistry registry;
  }

  public static final TemplateType TYPE = new TemplateType("mustache",
                                            "Mustache", "mustache");

 
  private static final Logger logger =
    LoggerFactory.getLogger(MustacheTemplateEngine.class);

  private final ServletMustacheFactory factory;

  @Inject
  public MustacheTemplateEngine(@Default ServletContext context, PluginLoaderHolder pluginLoaderHolder, MeterRegistryHolder registryHolder)
  {
    factory = new ServletMustacheFactory(context, createClassLoader(pluginLoaderHolder.pluginLoader));
    factory.setExecutorService(createExecutorService(registryHolder.registry));
  }

  private static ExecutorService createExecutorService(@Nullable MeterRegistry registry) {
    ExecutorService executorService = Executors.newCachedThreadPool(
      new ThreadFactoryBuilder()
        .setNameFormat("MustacheTemplateEngine-%d")
        .build()
    );

    if (registry != null) {
      Metrics.executor(registry, executorService,"MustacheTemplateEngine", "cached" );
    }

    return executorService;
  }

  private ClassLoader createClassLoader(PluginLoader pluginLoader) {
    if (pluginLoader == null) {
      return Thread.currentThread().getContextClassLoader();
    }
    return pluginLoader.getUberClassLoader();
  }


  @Override
  public Template getTemplate(String templateIdentifier, Reader reader) {
    if (logger.isTraceEnabled())
    {
      logger.trace("try to create mustache template from reader with id {}",
        templateIdentifier);
    }

    Mustache mustache = factory.compile(reader, templateIdentifier);

    return new MustacheTemplate(templateIdentifier, mustache);
  }


  @Override
  public Template getTemplate(String templatePath) throws IOException
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("try to find mustache template at {}", templatePath);
    }

    Template template = null;

    try
    {

      Mustache mustache = factory.compile(templatePath);

      if (mustache != null)
      {
        if (logger.isTraceEnabled())
        {
          logger.trace("return mustache template for {}", templatePath);
        }

        template = new MustacheTemplate(templatePath, mustache);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find mustache template at {}", templatePath);
      }

    }
    catch (MustacheTemplateNotFoundException ex)
    {
      logger.warn("could not find mustache template at {}", templatePath);
    }
    catch (UncheckedExecutionException | MustacheException ex)
    {
      handleWrappedException(ex, templatePath);
    }
    catch (Exception ex)
    {
      Throwables.propagateIfInstanceOf(ex, IOException.class);

      throw new TemplateParseException(
        "could not parse template for resource ".concat(templatePath), ex);
    }

    return template;
  }

  
  @Override
  public TemplateType getType()
  {
    return TYPE;
  }



  private void handleWrappedException(Exception ex, String templatePath)
    throws IOException
  {
    Throwable cause = ex.getCause();

    if (cause instanceof MustacheTemplateNotFoundException)
    {
      logger.warn("could not find mustache template at {}", templatePath);
    }
    else
    {
      Throwables.propagateIfInstanceOf(cause, IOException.class);

      throw new TemplateParseException(
        "could not parse template for resource ".concat(templatePath), cause);
    }
  }

}
