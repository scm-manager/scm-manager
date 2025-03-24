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


import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.Default;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * The {@link TemplateEngineFactory} is the entrypoint of the template api.
 * The factory return available template engines. The
 * {@link TemplateEngineFactory} is available via injection.
 *
 * @since 1.19
 */
@Singleton
public final class TemplateEngineFactory
{

  private static final Logger logger =
    LoggerFactory.getLogger(TemplateEngineFactory.class);

  /** default template engine */
  private final TemplateEngine defaultEngine;

  /** map of registered template engines */
  private final Map<String, TemplateEngine> engineMap;

  /**
   * Constructs new template engine factory. This constructor should only be
   * called from the injection framework.
   *
   *
   * @param engines Set of available template engines
   * @param defaultEngine default template engine
   */
  @Inject
  public TemplateEngineFactory(Set<TemplateEngine> engines,
    @Default TemplateEngine defaultEngine)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("register {} as default template engine",
        defaultEngine.getType());
    }

    engineMap = Maps.newHashMap();

    for (TemplateEngine engine : engines)
    {
      engineMap.put(engine.getType().getName(), engine);

      if (logger.isDebugEnabled())
      {
        logger.debug("register template engin {}", engine.getType());
      }
    }

    if (!engineMap.containsKey(defaultEngine.getType().getName()))
    {
      engineMap.put(defaultEngine.getType().getName(), defaultEngine);
    }

    this.defaultEngine = defaultEngine;
  }


  /**
   * Returns the default template engine. In the normal case this should be an
   * implementation of the
   * <a href="http://mustache.github.com/" target="_blank">mustache template
   * system</a>.
   *
   * @return default template engine
   */
  public TemplateEngine getDefaultEngine()
  {
    return defaultEngine;
  }

  /**
   * Returns template engine by its name. The name of a template engine can be
   * inquired by calling {@link TemplateType#getName()}. If no engine with the
   * given name registered this method will return null.
   *
   * @param engineName name of the engine
   *
   * @return engine with the given name or null
   */
  public TemplateEngine getEngine(String engineName)
  {
    return engineMap.get(engineName);
  }

  /**
   * Returns template engine by its extension. This method will use the
   * extension of the given path and search a template engine for this
   * extension. If no extension could be found in the the path, the method will
   * handle the whole path as a extension. If no engine could be found for the
   * given extension, this method will return null.
   *
   * @param path template path with extension
   *
   * @return template engine for the given path or null
   */
  public TemplateEngine getEngineByExtension(String path)
  {
    TemplateEngine engine = null;
    int index = path.lastIndexOf('.');

    if (index > 0)
    {
      path = path.substring(index + 1);
    }

    for (TemplateEngine e : engineMap.values())
    {
      if (e.getType().getExtensions().contains(path))
      {
        engine = e;

        break;
      }
    }

    return engine;
  }

  /**
   * Returns all registered template engines.
   */
  public Collection<TemplateEngine> getEngines()
  {
    return engineMap.values();
  }

}
