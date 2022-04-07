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

import com.google.common.io.Resources;
import picocli.CommandLine;
import sonia.scm.template.MustacheTemplateTestEngine;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import javax.servlet.ServletContext;
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

  public CommandLine.Model.CommandSpec getMockedSpeck() {
    CommandLine.Model.CommandSpec mock = mock(CommandLine.Model.CommandSpec.class);
    lenient().when(mock.resourceBundle()).thenReturn(getResourceBundle());
    return mock;
  }
}
