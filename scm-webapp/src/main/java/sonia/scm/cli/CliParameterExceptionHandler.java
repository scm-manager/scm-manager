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

import picocli.CommandLine;
import picocli.CommandLine.IParameterExceptionHandler;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CliParameterExceptionHandler implements IParameterExceptionHandler {

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



