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


import com.google.common.base.Stopwatch;
import com.google.inject.Binder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ConfigBinding;

import java.util.Set;
import java.util.stream.Collectors;


@SuppressWarnings("unchecked")
public class DefaultExtensionProcessor implements ExtensionProcessor {

 
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultExtensionProcessor.class);

  private final ExtensionCollector collector;
  private final Set<ConfigBinding> configBindings;

  public DefaultExtensionProcessor(ExtensionCollector collector, ConfigurationResolver configurationResolver) {
    this.collector = collector;
    this.configBindings = collector.getConfigElements().stream().map(configElement -> {
      String valueAsString = configurationResolver.resolve(configElement.getKey(), configElement.getDefaultValue());
      Object value = ConfigurationTypeConverter.convert(configElement.getType(), valueAsString);
      return new ConfigBinding(configElement, value);
    }).collect(Collectors.toSet());
  }



  @Override
  public Iterable<Class> byExtensionPoint(Class extensionPoint) {
    return collector.byExtensionPoint(extensionPoint);
  }


  @Override
  public Class oneByExtensionPoint(Class extensionPoint) {
    return collector.oneByExtensionPoint(extensionPoint);
  }


  @Override
  public void processAutoBindExtensions(Binder binder) {
    logger.info("start processing extensions");

    Stopwatch sw = Stopwatch.createStarted();

    new ExtensionBinder(binder).bind(collector);
    logger.info("bound extensions in {}", sw.stop());
  }



  @Override
  public Iterable<WebElementExtension> getWebElements() {
    return collector.getWebElements();
  }

  @Override
  public Iterable<Class<?>> getIndexedTypes() {
    return collector.getIndexedTypes();
  }

  @Override
  public Iterable<ConfigBinding> getConfigBindings() {
    return configBindings;
  }

}
