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
