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
    
package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.EagerSingleton;
import sonia.scm.util.Util;

/**
 *
 * @author Sebastian Sdorra
 */
@SuppressWarnings("unchecked")
public final class ExtensionBinder
{

  private static final String TYPE_LOOSE_EXT = "loose extension";
  private static final String TYPE_REST_RESOURCE = "rest resource";
  private static final String AS_EAGER_SINGLETON = " as eager singleton";

  /**
   * the logger for ExtensionBinder
   */
  private static final Logger logger = LoggerFactory.getLogger(ExtensionBinder.class);

  public ExtensionBinder(Binder binder)
  {
    this.binder = binder;
  }

  public void bind(ExtensionCollector collector)
  {
    logger.debug("bind extensions to extension points");

    for (ExtensionPointElement epe : collector.getExtensionPointElements())
    {
      bindExtensionPoint(collector, epe);
    }

    logger.debug("bind loose extensions");
    bindLooseExtensions(collector.getLooseExtensions());
    logger.debug("bind rest providers");
    bindRestProviders(collector.getRestProviders());
    logger.debug("bind rest resources");
    bindRestResource(collector.getRestResources());
  }

  private void bindExtensionPoint(ExtensionCollector collector,
    ExtensionPointElement epe)
  {
    if (epe.isAutoBind())
    {
      if (epe.isMultiple())
      {
        bindMultiExtensionPoint(epe, collector.byExtensionPoint(epe));
      }
      else
      {
        Class extension = collector.oneByExtensionPoint(epe);

        if (extension != null)
        {
          bindSingleInstance(epe, extension);
        }
        else
        {
          logger.warn("could not find extension for extension point {}",
            epe.getClazz());
        }
      }
    }
    else
    {
      logger.debug("bind type of {} is manual", epe.getClazz());
    }
  }

  private void bindLooseExtensions(Iterable<Class> extensions)
  {
    for (Class extension : extensions)
    {
      singleBind(TYPE_LOOSE_EXT, extension);
    }
  }

  private void bindMultiExtensionPoint(ExtensionPointElement extensionPoint, Iterable<Class> extensions)
  {
    Class extensionPointClass = extensionPoint.getClazz();

    logger.debug("create multibinder for {}", extensionPointClass.getName());

    Multibinder multibinder = Multibinder.newSetBinder(binder, extensionPointClass);
    for (Class extensionClass : extensions)
    {
      boolean eagerSingleton = isEagerSingleton(extensionClass);

      if (logger.isDebugEnabled())
      {
        String as = Util.EMPTY_STRING;

        if (eagerSingleton)
        {
          as = AS_EAGER_SINGLETON;
        }

        logger.debug("bind {} to multibinder of {}{}", extensionClass.getName(),
          extensionPointClass.getName(), as);
      }

      ScopedBindingBuilder sbb = multibinder.addBinding().to(extensionClass);

      if (eagerSingleton)
      {
        sbb.asEagerSingleton();
      }
    }
  }

  private void bindRestProviders(Iterable<Class> restProviders)
  {
    for (Class restProvider : restProviders)
    {
      logger.debug("bind rest provider {}", restProvider);
      binder.bind(restProvider).in(Singleton.class);
    }
  }

  private void bindRestResource(Iterable<Class> restResources)
  {
    for (Class restResource : restResources)
    {
      singleBind(TYPE_REST_RESOURCE, restResource);
    }
  }

  private void bindSingleInstance(ExtensionPointElement extensionPoint,
    Class extensionClass)
  {
    Class extensionPointClass = extensionPoint.getClazz();
    boolean eagerSingleton = isEagerSingleton(extensionClass);

    if (logger.isDebugEnabled())
    {
      String as = Util.EMPTY_STRING;

      if (eagerSingleton)
      {
        as = AS_EAGER_SINGLETON;
      }

      logger.debug("bind {} to {}{}", extensionClass.getName(),
        extensionPointClass.getName(), as);
    }

    ScopedBindingBuilder sbb =
      binder.bind(extensionPointClass).to(extensionClass);

    if (eagerSingleton)
    {
      sbb.asEagerSingleton();
    }
  }

  private void singleBind(String type, Class extension)
  {
    StringBuilder log = new StringBuilder();

    log.append("bind ").append(type).append(" ").append(extension);

    AnnotatedBindingBuilder abb = binder.bind(extension);

    if (isEagerSingleton(extension))
    {
      log.append(AS_EAGER_SINGLETON);
      abb.asEagerSingleton();
    }

    if (logger.isDebugEnabled()) {
      logger.debug(log.toString());
    }
  }

  private boolean isEagerSingleton(Class extensionClass)
  {
    return extensionClass.isAnnotationPresent(EagerSingleton.class);
  }

  private final Binder binder;
}
