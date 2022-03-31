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

package sonia.scm.cli;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateRendererTest {

  @Mock
  private CliContext context;
  @Mock
  private TemplateEngineFactory templateEngineFactory;
  @Mock
  private TemplateEngine engine;
  @Mock
  private Template template;

  @Test
  void shouldTemplateContentToStdout() throws IOException {
    when(context.getStdout()).thenReturn(new PrintWriter(new StringWriter()));
    when(templateEngineFactory.getDefaultEngine()).thenReturn(engine);
    when(engine.getTemplate(any(), any(StringReader.class))).thenReturn(template);
    TemplateRenderer templateRenderer = new TemplateRenderer(context, templateEngineFactory);
    templateRenderer.setSpec(CommandLine.Model.CommandSpec.create());

    templateRenderer.renderToStdout(":{{test}}!", ImmutableMap.of("test", "test_output"));

    verify(template).execute(any(), any());
  }

  @Test
  void shouldRenderErrorToStderr() throws IOException {
    when(context.getStderr()).thenReturn(new PrintWriter(new StringWriter()));
    when(templateEngineFactory.getDefaultEngine()).thenReturn(engine);
    when(engine.getTemplate(any(), any(StringReader.class))).thenReturn(template);
    TemplateRenderer templateRenderer = new TemplateRenderer(context, templateEngineFactory);
    templateRenderer.setSpec(CommandLine.Model.CommandSpec.create());

    templateRenderer.renderToStderr(":{{error}}!", ImmutableMap.of("error", "testerror"));

    verify(template).execute(any(), any());
  }

  @Test
  void shouldRenderDefaultErrorToStderr() throws IOException {
    when(context.getStderr()).thenReturn(new PrintWriter(new StringWriter()));
    when(templateEngineFactory.getDefaultEngine()).thenReturn(engine);
    when(engine.getTemplate(any(), any(StringReader.class))).thenReturn(template);
    TemplateRenderer templateRenderer = new TemplateRenderer(context, templateEngineFactory);
    templateRenderer.setSpec(CommandLine.Model.CommandSpec.create());

    templateRenderer.renderDefaultError("testerror");

    verify(template).execute(any(), any());
  }

  @Test
  void shouldRenderExceptionToStderr() throws IOException {
    when(context.getStderr()).thenReturn(new PrintWriter(new StringWriter()));
    when(templateEngineFactory.getDefaultEngine()).thenReturn(engine);
    when(engine.getTemplate(any(), any(StringReader.class))).thenReturn(template);
    TemplateRenderer templateRenderer = new TemplateRenderer(context, templateEngineFactory);
    templateRenderer.setSpec(CommandLine.Model.CommandSpec.create());

    templateRenderer.renderDefaultError(new RuntimeException("test"));

    verify(template).execute(any(), any());
  }
}
