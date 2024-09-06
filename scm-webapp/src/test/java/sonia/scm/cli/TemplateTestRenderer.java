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

import com.google.common.io.Resources;
import jakarta.servlet.ServletContext;
import picocli.CommandLine;
import sonia.scm.template.MustacheTemplateTestEngine;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.Collections.singleton;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is a helper to create a {@link TemplateRenderer} that can be used for
 * command tests
 */
public class TemplateTestRenderer {

  private final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
  private final ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
  private final CliContext context = mock(CliContext.class);
  private final TemplateEngineFactory templateEngineFactory;

  @SuppressWarnings("UnstableApiUsage")
  public TemplateTestRenderer() {
    lenient().when(context.getStdout()).thenReturn(new PrintWriter(stdOut));
    lenient().when(context.getStderr()).thenReturn(new PrintWriter(stdErr));
    lenient().doThrow(CliExitException.class).when(context).exit(anyInt());

    ServletContext servletContext = mock(ServletContext.class);
    lenient().when(servletContext.getResourceAsStream(any()))
      .thenAnswer(invocation -> Resources.getResource(invocation.getArgument(0, String.class)).openStream());
    TemplateEngine engine = new MustacheTemplateTestEngine().createEngine(servletContext);

    when(context.getLocale()).thenReturn(new Locale("en"));

    templateEngineFactory = new TemplateEngineFactory(singleton(engine), engine);
  }

  public TemplateRenderer createTemplateRenderer() {
    return new TemplateRenderer(context, templateEngineFactory) {
      @Override
      protected ResourceBundle getBundle() {
        return TemplateTestRenderer.this.getResourceBundle();
      }
    };
  }

  public ResourceBundle getResourceBundle() {
    return ResourceBundle.getBundle("sonia.scm.cli.i18n", context.getLocale(), new ResourceBundle.Control() {
      @Override
      public Locale getFallbackLocale(String baseName, Locale locale) {
        return Locale.ROOT;
      }
    });
  }

  public void setLocale(String locale) {
    lenient().when(context.getLocale()).thenReturn(new Locale(locale));
  }

  public String getStdOut() {
    return stdOut.toString();
  }

  public String getStdErr() {
    return stdErr.toString();
  }

  public CliContext getContextMock() {
    return context;
  }

  public TemplateEngineFactory getTemplateEngineFactory() {
    return templateEngineFactory;
  }

  public CommandLine.Model.CommandSpec getMockedSpec() {
    ResourceBundle resourceBundle = getResourceBundle();
    CommandLine.Model.CommandSpec mock = mock(CommandLine.Model.CommandSpec.class);
    lenient().when(mock.resourceBundle()).thenReturn(resourceBundle);
    return mock;
  }
}
