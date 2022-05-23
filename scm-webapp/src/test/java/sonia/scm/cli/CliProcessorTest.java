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

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import sonia.scm.plugin.PluginLoader;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CliProcessorTest {

  @Mock
  private CommandRegistry registry;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CliContext context;

  @Mock
  private PluginLoader pluginLoader;
  @Mock
  private CliExceptionHandlerFactory exceptionHandlerFactory;
  @Mock
  private CliExecutionExceptionHandler executionExceptionHandler;
  @Mock
  private CliParameterExceptionHandler parameterExceptionHandler;

  @BeforeEach
  void mockPluginLoader() {
    when(pluginLoader.getUberClassLoader()).thenReturn(this.getClass().getClassLoader());
  }

  @Nested
  class ForDefaultLanguageTest {

    @BeforeEach
    void setDefaultLocale() {
      when(context.getLocale()).thenReturn(Locale.ENGLISH);
      when(exceptionHandlerFactory.createExecutionExceptionHandler("en")).thenReturn(executionExceptionHandler);
      when(exceptionHandlerFactory.createParameterExceptionHandler("en")).thenReturn(parameterExceptionHandler);
    }

    @Test
    void shouldExecutePingCommand() {
      when(registry.createCommandTree()).thenReturn(ImmutableList.of(new RegisteredCommandNode("ping", PingCommand.class)));
      Injector injector = Guice.createInjector();
      CliProcessor cliProcessor = new CliProcessor(registry, injector, exceptionHandlerFactory, pluginLoader);

      cliProcessor.execute(context, "ping");

      verify(context.getStdout()).println("PONG");
    }

    @Test
    void shouldExecutePingCommandWithExitCode0() {
      when(registry.createCommandTree()).thenReturn(ImmutableList.of(new RegisteredCommandNode("ping", PingCommand.class)));
      Injector injector = Guice.createInjector();
      CliProcessor cliProcessor = new CliProcessor(registry, injector, exceptionHandlerFactory, pluginLoader);

      int exitCode = cliProcessor.execute(context, "ping");

      assertThat(exitCode).isZero();
    }

    @Test
    void shouldPrintCommandOne() {
      String result = executeHierarchyCommands("--help");

      assertThat(result).contains(format("Commands:%n" +
        "  one"));
    }

    @Test
    void shouldPrintCommandTwo() {
      String result = executeHierarchyCommands("one", "--help");

      assertThat(result).contains(format("Commands:%n" +
        "  two"));
    }

    @Test
    void shouldPrintCommandThree() {
      String result = executeHierarchyCommands("one", "two", "--help");

      assertThat(result).contains(format("Commands:%n" +
        "  three"));
    }
  }

  @Nested
  class ForAnotherLanguageTest {

    @Mock
    private CliExecutionExceptionHandler germanExecutionExceptionHandler;
    @Mock
    private CliParameterExceptionHandler germanParamExceptionHandler;

    @BeforeEach
    void setUpOtherLanguage() {
      when(exceptionHandlerFactory.createParameterExceptionHandler("de")).thenReturn(germanParamExceptionHandler);
      when(exceptionHandlerFactory.createExecutionExceptionHandler("de")).thenReturn(germanExecutionExceptionHandler);
      when(context.getLocale()).thenReturn(Locale.GERMAN);
    }

    @Test
    void shouldUseResourceBundleFromAnnotationWithContextLocale() {
      String helpForThree = executeHierarchyCommands("one", "two", "three", "--help");

      assertThat(helpForThree).contains("Dies ist meine App.");
    }

    @Test
    void shouldUseDefaultWithoutResourceBundle() {
      String helpForTwo = executeHierarchyCommands("one", "two", "--help");

      assertThat(helpForTwo).contains("Dies ist meine App.");
    }

    @Test
    void shouldUseExceptionHandlerForOtherLanguage() {
      executeHierarchyCommands("one", "two", "--help");

      verify(exceptionHandlerFactory).createExecutionExceptionHandler("de");
      verify(exceptionHandlerFactory).createParameterExceptionHandler("de");
    }
  }

  @Nonnull
  private String executeHierarchyCommands(String... args) {
    RegisteredCommandNode one = new RegisteredCommandNode("one", RootCommand.class);
    RegisteredCommandNode two = new RegisteredCommandNode("two", SubCommand.class);
    RegisteredCommandNode three = new RegisteredCommandNode("three", SubSubCommand.class);
    two.getChildren().add(three);
    one.getChildren().add(two);

    when(registry.createCommandTree()).thenReturn(ImmutableList.of(one));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    when(context.getStdout()).thenReturn(new PrintWriter(baos));

    Injector injector = Guice.createInjector();
    CliProcessor cliProcessor = new CliProcessor(registry, injector, exceptionHandlerFactory, pluginLoader);

    cliProcessor.execute(context, args);
    return baos.toString();
  }

  @CommandLine.Command(name = "one")
  static class RootCommand implements Runnable {

    @Override
    public void run() {

    }
  }

  @CommandLine.Command(name = "two")
  static class SubCommand implements Runnable {

    @Override
    public void run() {

    }
  }

  @CommandLine.Command(name = "three")
  @CliResourceBundle("sonia.scm.cli.test")
  static class SubSubCommand implements Runnable {
    @Override
    public void run() {

    }
  }
}
