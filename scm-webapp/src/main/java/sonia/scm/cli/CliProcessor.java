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
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Locale;
import java.util.ResourceBundle;

public class CliProcessor {

  private final CommandRegistry registry;
  private final Injector injector;

  @Inject
  public CliProcessor(CommandRegistry registry, Injector injector) {
    this.registry = registry;
    this.injector = injector;
  }

  public int execute(CliContext context, String... args) {
    CommandFactory factory = new CommandFactory(injector, context);
    CommandLine cli = new CommandLine(ScmManagerCommand.class, factory);
    cli.setResourceBundle(ResourceBundle.getBundle("sonia.scm.cli.i18n", context.getLocale()));
    for (RegisteredCommandNode c : registry.createCommandTree()) {
      CommandLine commandline = createCommandline(context, factory, c);
      cli.getCommandSpec().addSubcommand(c.getName(), commandline);
    }
    cli.setErr(context.getStderr());
    cli.setOut(context.getStdout());
    cli.getCommandSpec().resourceBundle();
    return cli.execute(args);
  }

  private CommandLine createCommandline(CliContext context, CommandFactory factory, RegisteredCommandNode command) {
    CommandLine commandLine = new CommandLine(command.getCommand(), factory);

    ResourceBundle resourceBundle = commandLine.getCommandSpec().resourceBundle();
    if (resourceBundle != null) {
      String resourceBundleBaseName = resourceBundle.getBaseBundleName();
      commandLine.setResourceBundle(ResourceBundle.getBundle(resourceBundleBaseName, context.getLocale()));
    }
    for (RegisteredCommandNode child : command.getChildren()) {
      if (!commandLine.getCommandSpec().subcommands().containsKey(child.getName())) {
        CommandLine childCommandLine = createCommandline(context, factory, child);
        commandLine.getCommandSpec().addSubcommand(child.getName(), childCommandLine);
      }
    }

    return commandLine;
  }
}
