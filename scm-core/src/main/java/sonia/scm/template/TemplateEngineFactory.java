/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.template;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @DefaultEngine TemplateEngine defaultEngine)
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
  private TemplateEngine defaultEngine;

  /** map of registered template engines */
  private Map<String, TemplateEngine> engineMap;
}
