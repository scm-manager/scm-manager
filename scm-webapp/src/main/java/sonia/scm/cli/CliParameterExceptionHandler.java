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

import picocli.CommandLine;
import picocli.CommandLine.IParameterExceptionHandler;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

class CliParameterExceptionHandler implements IParameterExceptionHandler {

  private final String languageCode;

  public CliParameterExceptionHandler(String languageCode) {
    this.languageCode = languageCode;
  }

  @Override
  public int handleParseException(CommandLine.ParameterException ex, String[] args) {
    CommandLine cmd = ex.getCommandLine();
    PrintWriter stderr = cmd.getErr();

    if (languageCode.equals("en")) {
      // Do not use translated exceptions for english since the default ones are more specific
      printError(stderr, ex.getMessage());
      if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, stderr)) {
        ex.getCommandLine().usage(stderr);
      }
    } else {
      handleTranslatedExceptions(ex, stderr);
    }
    return ExitCode.USAGE;
  }

  private void handleTranslatedExceptions(CommandLine.ParameterException ex, PrintWriter stderr) {
    if (ex.getCause() instanceof CommandLine.TypeConversionException) {
      printError(stderr, getMessage("exception.typeConversion") + ex.getArgSpec().paramLabel());
    }
    if (ex instanceof CommandLine.MissingParameterException) {
      printError(stderr, getMessage("exception.missingParameter") +
        ((CommandLine.MissingParameterException) ex).getMissing()
          .stream()
          .map(CommandLine.Model.ArgSpec::paramLabel)
          .collect(Collectors.toList()));
    }
    if (ex instanceof CommandLine.OverwrittenOptionException) {
      printError(stderr, getMessage("exception.overwrittenOption")
        + ((CommandLine.OverwrittenOptionException) ex).getOverwritten().paramLabel());
    }
    if (ex instanceof CommandLine.MaxValuesExceededException) {
      printError(stderr, getMessage("exception.maxValuesExceeded"));
    }
    if (ex instanceof CommandLine.MutuallyExclusiveArgsException) {
      printError(stderr, getMessage("exception.mutuallyExclusiveArgs"));
    }
    if (ex instanceof CommandLine.UnmatchedArgumentException) {
      printError(stderr, getMessage("exception.unmatchedArguments")
        + ((CommandLine.UnmatchedArgumentException) ex).getUnmatched());
    }
    if (ex.getMessage().equals("Missing required subcommand")) {
      printError(stderr, getMessage("exception.missingSubCommand"));
    }

    // Print command description or suggestions to avoid this error
    if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, stderr)) {
      ex.getCommandLine().usage(stderr);
    }
  }

  private void printError(PrintWriter stderr, String message) {
    stderr.println(getMessage("errorLabel") + ": " + message);
  }

  private String getMessage(String key) {
    return ResourceBundle
      .getBundle("sonia.scm.cli.i18n", new Locale(languageCode))
      .getString(key);
  }
}



