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

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.Default;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * The {@link TemplateEngineFactory} is the entrypoint of the template api.
 * The factory return available template engines. The
 * {@link TemplateEngineFactory} is available via injection.
 *
 * @author Sebastian Sdorra
 * @since 1.19
 *
 * @apiviz.landmark
 * @apiviz.uses sonia.scm.template.TemplateEngine
 */
@Singleton
public final class TemplateEngineFactory
{

  /**
   * the logger for TemplateEngineFactory
   */
  private static final Logger logger =
    LoggerFactory.getLogger(TemplateEngineFactory.class);

  //~--- constructors ---------------------------------------------------------

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

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the default template engine. In the normal case the should be a
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
   *
   *
   * @return all registered template engines
   */
  public Collection<TemplateEngine> getEngines()
  {
    return engineMap.values();
  }

  //~--- fields ---------------------------------------------------------------

  /** default template engine */
  private final TemplateEngine defaultEngine;

  /** map of registered template engines */
  private final Map<String, TemplateEngine> engineMap;
}
