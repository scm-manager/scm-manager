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
