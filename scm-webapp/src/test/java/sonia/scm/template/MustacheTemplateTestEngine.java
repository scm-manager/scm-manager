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

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import sonia.scm.plugin.PluginLoader;

import javax.servlet.ServletContext;

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
