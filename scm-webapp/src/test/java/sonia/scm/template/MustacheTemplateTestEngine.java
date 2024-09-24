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

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.ServletContext;
import sonia.scm.plugin.PluginLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class can be used to create a mustache renderer in unit test
 */
public class MustacheTemplateTestEngine {

  public TemplateEngine createEngine(ServletContext context) {
    PluginLoader loader = mock(PluginLoader.class);

    when(loader.getUberClassLoader()).thenReturn(
      Thread.currentThread().getContextClassLoader());

    MustacheTemplateEngine.PluginLoaderHolder pluginLoaderHolder = new MustacheTemplateEngine.PluginLoaderHolder();
    pluginLoaderHolder.pluginLoader = loader;

    MustacheTemplateEngine.MeterRegistryHolder meterRegistryHolder = new MustacheTemplateEngine.MeterRegistryHolder();
    meterRegistryHolder.registry = new SimpleMeterRegistry();

    return new MustacheTemplateEngine(context, pluginLoaderHolder, meterRegistryHolder);
  }
}
