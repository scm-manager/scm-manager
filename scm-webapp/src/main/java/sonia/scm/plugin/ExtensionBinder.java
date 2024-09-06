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

package sonia.scm.plugin;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.util.Util;


@SuppressWarnings({"unchecked", "rawtypes"})
public final class ExtensionBinder {

  private static final String TYPE_LOOSE_EXT = "loose extension";
  private static final String TYPE_REST_RESOURCE = "rest resource";
  private static final String AS_EAGER_SINGLETON = " as eager singleton";

  private static final Logger logger = LoggerFactory.getLogger(ExtensionBinder.class);

  public ExtensionBinder(Binder binder) {
    this.binder = binder;
  }

  public void bind(ExtensionCollector collector) {
    logger.debug("bind extensions to extension points");

    for (ExtensionPointElement epe : collector.getExtensionPointElements()) {
      bindExtensionPoint(collector, epe);
    }

    logger.debug("bind loose extensions");
    bindLooseExtensions(collector.getLooseExtensions());
    logger.debug("bind rest providers");
    bindRestProviders(collector.getRestProviders());
    logger.debug("bind rest resources");
    bindRestResource(collector.getRestResources());
    logger.debug("bind mapper modules");
    bindMapperModules(collector.getMappers());
  }

  private void bindExtensionPoint(ExtensionCollector collector,
                                  ExtensionPointElement epe) {
    if (epe.isAutoBind()) {
      if (epe.isMultiple()) {
        bindMultiExtensionPoint(epe, collector.byExtensionPoint(epe));
      } else {
        Class extension = collector.oneByExtensionPoint(epe);

        if (extension != null) {
          bindSingleInstance(epe, extension);
        } else {
          logger.warn("could not find extension for extension point {}",
            epe.getClazz());
        }
      }
    } else {
      logger.debug("bind type of {} is manual", epe.getClazz());
    }
  }

  private void bindLooseExtensions(Iterable<Class> extensions) {
    for (Class extension : extensions) {
      singleBind(TYPE_LOOSE_EXT, extension);
    }
  }

  private void bindMultiExtensionPoint(ExtensionPointElement extensionPoint, Iterable<Class> extensions) {
    Class extensionPointClass = extensionPoint.getClazz();

    logger.debug("create multibinder for {}", extensionPointClass.getName());

    Multibinder multibinder = Multibinder.newSetBinder(binder, extensionPointClass);
    for (Class extensionClass : extensions) {
      boolean eagerSingleton = isEagerSingleton(extensionClass);

      if (logger.isDebugEnabled()) {
        String as = Util.EMPTY_STRING;

        if (eagerSingleton) {
          as = AS_EAGER_SINGLETON;
        }

        logger.debug("bind {} to multibinder of {}{}", extensionClass.getName(),
          extensionPointClass.getName(), as);
      }

      ScopedBindingBuilder sbb = multibinder.addBinding().to(extensionClass);

      if (eagerSingleton) {
        sbb.asEagerSingleton();
      }
    }
  }

  private void bindRestProviders(Iterable<Class> restProviders) {
    for (Class restProvider : restProviders) {
      logger.debug("bind rest provider {}", restProvider);
      binder.bind(restProvider).in(Singleton.class);
    }
  }

  private void bindRestResource(Iterable<Class> restResources) {
    for (Class restResource : restResources) {
      singleBind(TYPE_REST_RESOURCE, restResource);
    }
  }

  private void bindMapperModules(Iterable<Class> mapperModules) {
    for (Class mapperModule : mapperModules) {
      binder.bind(mapperModule).to(Mappers.getMapperClass(mapperModule));
    }
  }

  private void bindSingleInstance(ExtensionPointElement extensionPoint,
                                  Class extensionClass) {
    Class extensionPointClass = extensionPoint.getClazz();
    boolean eagerSingleton = isEagerSingleton(extensionClass);

    if (logger.isDebugEnabled()) {
      String as = Util.EMPTY_STRING;

      if (eagerSingleton) {
        as = AS_EAGER_SINGLETON;
      }

      logger.debug("bind {} to {}{}", extensionClass.getName(),
        extensionPointClass.getName(), as);
    }

    ScopedBindingBuilder sbb =
      binder.bind(extensionPointClass).to(extensionClass);

    if (eagerSingleton) {
      sbb.asEagerSingleton();
    }
  }

  private void singleBind(String type, Class extension) {
    StringBuilder log = new StringBuilder();

    log.append("bind ").append(type).append(" ").append(extension);

    AnnotatedBindingBuilder abb = binder.bind(extension);

    if (isEagerSingleton(extension)) {
      log.append(AS_EAGER_SINGLETON);
      abb.asEagerSingleton();
    }

    if (logger.isDebugEnabled()) {
      logger.debug(log.toString());
    }
  }

  private boolean isEagerSingleton(Class extensionClass) {
    return extensionClass.isAnnotationPresent(EagerSingleton.class);
  }

  private final Binder binder;
}
