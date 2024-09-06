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


import com.google.common.collect.ImmutableMap;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.ServletContext;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import sonia.scm.plugin.PluginLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MustacheTemplateEngineTest extends TemplateEngineTestBase
{


  @Override
  public TemplateEngine createEngine(ServletContext context)
  {
    PluginLoader loader = mock(PluginLoader.class);

    when(loader.getUberClassLoader()).thenReturn(
      Thread.currentThread().getContextClassLoader());

    MustacheTemplateEngine.PluginLoaderHolder pluginLoaderHolder = new MustacheTemplateEngine.PluginLoaderHolder();
    pluginLoaderHolder.pluginLoader = loader;

    MustacheTemplateEngine.MeterRegistryHolder meterRegistryHolder = new MustacheTemplateEngine.MeterRegistryHolder();
    meterRegistryHolder.registry = new SimpleMeterRegistry();

    return new MustacheTemplateEngine(context, pluginLoaderHolder, meterRegistryHolder);
  }



  @Override
  public String getDefectTemplateResource()
  {
    return "sonia/scm/template/005.mustache";
  }


  @Override
  public String getTemplateResource()
  {
    return "sonia/scm/template/001.mustache";
  }

  @Override
  public String getTemplateResourceWithGermanTranslation() {
    return "sonia/scm/template/loc.mustache";
  }


  @Override
  protected InputStream getResource(String resource)
  {
    return MustacheTemplateEngineTest.class.getResourceAsStream(
      "/sonia/scm/template/".concat(resource).concat(".mustache"));
  }

  @Test
  public void testCreateEngineWithoutPluginLoader() throws IOException {
    ServletContext context = mock(ServletContext.class);
    MustacheTemplateEngine.PluginLoaderHolder pluginLoaderHolder = new MustacheTemplateEngine.PluginLoaderHolder();
    MustacheTemplateEngine.MeterRegistryHolder meterRegistryHolder = new MustacheTemplateEngine.MeterRegistryHolder();
    MustacheTemplateEngine engine = new MustacheTemplateEngine(context, pluginLoaderHolder, meterRegistryHolder);

    Template template = engine.getTemplate(getTemplateResource());

    StringWriter writer = new StringWriter();
    template.execute(writer, ImmutableMap.of("name", "World"));

    Assertions.assertThat(writer).hasToString("Hello World!");
  }
}
