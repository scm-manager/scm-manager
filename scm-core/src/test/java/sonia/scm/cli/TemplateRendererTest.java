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
