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

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import sonia.scm.ExceptionWithContext;
import sonia.scm.TransactionId;
import sonia.scm.i18n.I18nCollector;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Slf4j
class CliExecutionExceptionHandler implements CommandLine.IExecutionExceptionHandler {

  private final I18nCollector i18nCollector;
  private final String languageCode;

  CliExecutionExceptionHandler(I18nCollector i18nCollector, String languageCode) {
    this.i18nCollector = i18nCollector;
    this.languageCode = languageCode;
  }

  @Override
  public int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult) {
    log.debug("got exception in cli execution", ex);
    if (ex instanceof CliExitException) {
      return ((CliExitException) ex).getExitCode();
    }

    PrintWriter stdErr = commandLine.getErr();
    stdErr.print(getMessage("errorExecutionFailed"));
    stdErr.print(": ");
    String message = ex.getMessage();
    if (ex instanceof ExceptionWithContext) {
      String code = ((ExceptionWithContext) ex).getCode();
      message = getMessageFromI18nBundle(code).orElse(message);
    }
    stdErr.println(message);
    TransactionId.get().ifPresent(transactionId -> stdErr.println(getMessage("transactionId") + ": " + transactionId));
    stdErr.println();
    commandLine.usage(stdErr);
    return ExitCode.SERVER_ERROR;
  }

  private String getMessage(String key) {
    return ResourceBundle
      .getBundle("sonia.scm.cli.i18n", new Locale(languageCode))
      .getString(key);
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
