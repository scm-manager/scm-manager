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

import com.google.inject.Injector;
import jakarta.inject.Inject;
import picocli.AutoComplete;
import picocli.CommandLine;
import sonia.scm.plugin.PluginLoader;

import java.util.Locale;
import java.util.ResourceBundle;

public class CliProcessor {

  private static final String CORE_RESOURCE_BUNDLE = "sonia.scm.cli.i18n";
  private final CommandRegistry registry;
  private final Injector injector;
  private final CommandLine.Model.CommandSpec usageHelp;
  private final CliExceptionHandlerFactory exceptionHandlerFactory;
  private final PluginLoader pluginLoader;

  @Inject
  public CliProcessor(CommandRegistry registry, Injector injector, CliExceptionHandlerFactory exceptionHandlerFactory, PluginLoader pluginLoader) {
    this.registry = registry;
    this.injector = injector;
    this.exceptionHandlerFactory = exceptionHandlerFactory;
    this.pluginLoader = pluginLoader;
    this.usageHelp = new CommandLine(HelpMixin.class).getCommandSpec();
  }

  public int execute(CliContext context, String... args) {
    CommandFactory factory = new CommandFactory(injector, context);
    CommandLine cli = new CommandLine(ScmManagerCommand.class, factory);
    cli.getCommandSpec().addMixin("help", usageHelp);
    cli.setResourceBundle(getBundle(CORE_RESOURCE_BUNDLE, context.getLocale()));
    for (RegisteredCommandNode c : registry.createCommandTree()) {
      CommandLine commandline = createCommandline(context, factory, c);
      cli.getCommandSpec().addSubcommand(c.getName(), commandline);
    }
    cli.addSubcommand(AutoComplete.GenerateCompletion.class);
    cli.setErr(context.getStderr());
    cli.setOut(context.getStdout());
    cli.setExecutionExceptionHandler(exceptionHandlerFactory.createExecutionExceptionHandler(context.getLocale().getLanguage()));
    cli.setParameterExceptionHandler(exceptionHandlerFactory.createParameterExceptionHandler(context.getLocale().getLanguage()));
    return cli.execute(args);
  }

  private CommandLine createCommandline(CliContext context, CommandFactory factory, RegisteredCommandNode command) {
    CommandLine commandLine = new CommandLine(command.getCommand(), factory);
    commandLine.getCommandSpec().addMixin("help", usageHelp);

    CliResourceBundle customResourceBundle = command.getCommand().getAnnotation(CliResourceBundle.class);
    if (customResourceBundle != null) {
      String resourceBundleBaseName = customResourceBundle.value();
      ResourceBundle pluginResourceBundle = getBundle(resourceBundleBaseName, context.getLocale());
      ResourceBundle coreResourceBundle = getBundle(CORE_RESOURCE_BUNDLE, context.getLocale());
      CombinedResourceBundle combinedResourceBundle = new CombinedResourceBundle(pluginResourceBundle, coreResourceBundle);
      commandLine.setResourceBundle(combinedResourceBundle);
    }
    for (RegisteredCommandNode child : command.getChildren()) {
      if (!commandLine.getCommandSpec().subcommands().containsKey(child.getName())) {
        CommandLine childCommandLine = createCommandline(context, factory, child);
        commandLine.getCommandSpec().addSubcommand(child.getName(), childCommandLine);
      }
    }

    return commandLine;
  }

  private ResourceBundle getBundle(String baseName, Locale locale) {
    return ResourceBundle.getBundle(baseName, locale, pluginLoader.getUberClassLoader(), new ResourceBundle.Control() {
      @Override
      public Locale getFallbackLocale(String baseName, Locale locale) {
        return Locale.ROOT;
      }
    });
  }
}
