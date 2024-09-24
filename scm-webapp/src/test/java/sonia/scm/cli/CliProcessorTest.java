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

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import sonia.scm.i18n.I18nCollector;
import sonia.scm.plugin.PluginLoader;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
  @Mock
  private PermissionDescriptionResolverFactory permissionDescriptionResolverFactory;

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
      Injector injector = Guice.createInjector(new MockedModule());
      CliProcessor cliProcessor = new CliProcessor(registry, injector, exceptionHandlerFactory, pluginLoader);

      cliProcessor.execute(context, "ping");

      verify(context.getStdout()).println("PONG");
    }

    @Test
    void shouldExecutePingCommandWithExitCode0() {
      when(registry.createCommandTree()).thenReturn(ImmutableList.of(new RegisteredCommandNode("ping", PingCommand.class)));
      Injector injector = Guice.createInjector(new MockedModule());
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

    Injector injector = Guice.createInjector(new MockedModule());
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

  static class MockedModule implements Module {

    @Override
    public void configure(Binder binder) {
      I18nCollector i18nCollector = mock(I18nCollector.class);
      binder.bind(PermissionDescriptionResolverFactory.class)
        .toInstance(new PermissionDescriptionResolverFactory(i18nCollector));
    }
  }
}
