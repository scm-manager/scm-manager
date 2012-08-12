/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
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
 *
 * @author Sebastian Sdorra
 * @since 1.19
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
   * Constructs ...
   *
   *
   * @param engines
   * @param defaultEngine
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
   * Method description
   *
   *
   * @return
   */
  public TemplateEngine getDefaultEngine()
  {
    return defaultEngine;
  }

  /**
   * Method description
   *
   *
   * @param engineName
   *
   * @return
   */
  public TemplateEngine getEngine(String engineName)
  {
    return engineMap.get(engineName);
  }

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  public TemplateEngine getEngineByExtension(String path)
  {
    TemplateEngine engine = null;
    int index = path.lastIndexOf('.');

    if (index > 0)
    {
      path = path.substring(index);
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
   * Method description
   *
   *
   * @return
   */
  public Collection<TemplateEngine> getEngines()
  {
    return engineMap.values();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private TemplateEngine defaultEngine;

  /** Field description */
  private Map<String, TemplateEngine> engineMap;
}
