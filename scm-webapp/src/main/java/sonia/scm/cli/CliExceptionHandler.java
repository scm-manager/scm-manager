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

import com.fasterxml.jackson.databind.JsonNode;
import picocli.CommandLine;
import sonia.scm.ExceptionWithContext;
import sonia.scm.i18n.I18nCollector;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class CliExceptionHandler implements CommandLine.IExecutionExceptionHandler {

  private final I18nCollector i18nCollector;
  private final String languageCode;

  CliExceptionHandler(I18nCollector i18nCollector, String languageCode) {
    this.i18nCollector = i18nCollector;
    this.languageCode = languageCode;
  }

  @Override
  public int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult) {
    if (ex instanceof CliExitException) {
      return ((CliExitException) ex).getExitCode();
    }

    PrintWriter stdErr = commandLine.getErr();
    stdErr.print(getErrorExecutionFailedMessage());
    String message = ex.getMessage();
    if (ex instanceof ExceptionWithContext) {
      String code = ((ExceptionWithContext) ex).getCode();
      message = getMessageFromI18nBundle(code).orElse(message);
      stdErr.print(" " + code);
    }
    stdErr.print(": ");
    stdErr.println(message);
    stdErr.println();
    commandLine.usage(stdErr);
    return ExitCode.SERVER_ERROR;
  }

  private String getErrorExecutionFailedMessage() {
    return ResourceBundle
      .getBundle("sonia.scm.cli.i18n", new Locale(languageCode))
      .getString("errorExecutionFailed");
  }

  private Optional<String> getMessageFromI18nBundle(String code) {
    Optional<String> translatedMessage = getMessageFromI18nBundle(code, languageCode);
    if (translatedMessage.isPresent()) {
      return translatedMessage;
    } else {
      return getMessageFromI18nBundle(code, "en");
    }
  }

  private Optional<String> getMessageFromI18nBundle(String code, String languageCode) {
    Optional<JsonNode> jsonNode;
    try {
      jsonNode = i18nCollector.findJson(languageCode);
    } catch (IOException e) {
      return empty();
    }
    if (!jsonNode.isPresent()) {
      return empty();
    }
    JsonNode errorsNode = jsonNode.get().get("errors");
    if (errorsNode == null) {
      return empty();
    }
    JsonNode codeNode = errorsNode.get(code);
    if (codeNode == null) {
      return empty();
    }
    JsonNode descriptionNode = codeNode.get("description");
    if (descriptionNode == null) {
      return empty();
    }
    return of(descriptionNode.asText());
  }
}
