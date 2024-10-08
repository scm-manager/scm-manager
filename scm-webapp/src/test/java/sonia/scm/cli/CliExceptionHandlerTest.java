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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.DefaultLocale;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import sonia.scm.AlreadyExistsException;
import sonia.scm.ContextEntry;
import sonia.scm.NotFoundException;
import sonia.scm.TransactionId;
import sonia.scm.group.Group;
import sonia.scm.i18n.I18nCollector;
import sonia.scm.store.StoreReadOnlyException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CliExceptionHandlerTest {

  private static final String PLUGIN_BUNDLE_EN = "{" +
    "  \"errors\": {" +
    "    \"AGR7UzkhA1\": {" +
    "      \"displayName\": \"Not found\"," +
    "      \"description\": \"The requested entity could not be found. It may have been deleted in another session.\"" +
    "    }," +
    "    \"3FSIYtBJw1\": {" +
    "      \"displayName\": \"An entity could not be stored\"" +
    "    }" +
    "  }" +
    "}";

  private static final String PLUGIN_BUNDLE_DE = "{" +
    "  \"errors\": {" +
    "    \"3FSIYtBJw1\": {" +
    "      \"displayName\": \"Ein Datensatz konnte nicht gespeichert werden\"," +
    "      \"description\": \"Ein Datensatz konnte nicht gespeichert werden, da der entsprechende Speicher als schreibgeschützt markiert wurde. Weitere Hinweise finden sich im Log.\"" +
    "    }" +
    "  }" +
    "}";

  @Mock
  private I18nCollector i18nCollector;

  @Mock
  private CommandLine commandLine;

  private CliExecutionExceptionHandler executionExceptionHandler;
  private CliParameterExceptionHandler parameterExceptionHandler;

  private final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
  private final PrintWriter stdErr = new PrintWriter(errorStream);

  @BeforeEach
  void initErrorStream() {
    when(commandLine.getErr()).thenReturn(stdErr);
  }

  @Nested
  @DefaultLocale("en")
  class EnglishLanguageTest {

    @BeforeEach
    void setUpHandler() {
      executionExceptionHandler = new CliExecutionExceptionHandler(i18nCollector, "en");
      parameterExceptionHandler = new CliParameterExceptionHandler("en");
    }

    @Test
    void shouldPrintSimpleException() {
      int exitCode = callExecHandler(new NullPointerException("expected error"));

      assertThat(errorStream.toString()).contains("Error: expected error");
      assertThat(exitCode).isEqualTo(1);
    }

    @Test
    void shouldPrintTransactionIdIfPresent() {
      TransactionId.set("42");

      callExecHandler(new NullPointerException("expected error"));

      assertThat(errorStream.toString()).contains("Transaction ID: 42");
    }

    @Test
    void shouldUseTranslatedMessageFromExceptionWithContext() throws IOException {
      when(i18nCollector.findJson("en"))
        .thenReturn(Optional.of(new ObjectMapper().readTree(PLUGIN_BUNDLE_EN)));

      callExecHandler(NotFoundException.notFound(new ContextEntry.ContextBuilder()));

      assertThat(errorStream.toString()).contains("Error: The requested entity could not be found. It may have been deleted in another session.");
    }

    @Test
    void shouldUseExceptionMessageForExceptionWithContextWithoutCodeNode() throws IOException {
      when(i18nCollector.findJson("en"))
        .thenReturn(Optional.of(new ObjectMapper().readTree(PLUGIN_BUNDLE_EN)));

      callExecHandler(new AlreadyExistsException(new Group("test", "hog")));

      assertThat(errorStream.toString()).contains("Error: group with id hog already exists");
    }

    @Test
    void shouldUseExceptionMessageForExceptionWithContextWithoutDescriptionNode() throws IOException {
      when(i18nCollector.findJson("en"))
        .thenReturn(Optional.of(new ObjectMapper().readTree(PLUGIN_BUNDLE_EN)));

      callExecHandler(new StoreReadOnlyException("test"));

      assertThat(errorStream.toString()).contains("Error: Store is read only, could not write location test");
    }

    @Test
    void shouldUseParameterExceptionHandlerWithDefaultExceptionMessages() {
      callParamHandler(new CommandLine.OverwrittenOptionException(commandLine, null, "My default exception which will not be overwritten"), "");

      assertThat(errorStream.toString()).contains("ERROR: My default exception which will not be overwritten");
    }
  }

  @Nested
  @DefaultLocale("de")
  class GermanLanguageTest {

    @BeforeEach
    void setUpHandler() {
      executionExceptionHandler = new CliExecutionExceptionHandler(i18nCollector, "de");
      parameterExceptionHandler = new CliParameterExceptionHandler("de");
    }

    @Test
    void shouldPrintSimpleException() {
      int exitCode = callExecHandler(new NullPointerException("expected error"));

      assertThat(errorStream.toString()).contains("Fehler: expected error");
      assertThat(exitCode).isEqualTo(1);
    }


    @Test
    void shouldPrintTransactionIdIfPresent() {
      TransactionId.set("42");

      callExecHandler(new NullPointerException("expected error"));

      assertThat(errorStream.toString()).contains("Transaktions-ID: 42");
    }

    @Test
    void shouldUseTranslatedMessageFromExceptionWithContext() throws IOException {
      when(i18nCollector.findJson("de"))
        .thenReturn(Optional.of(new ObjectMapper().readTree(PLUGIN_BUNDLE_DE)));

      callExecHandler(new StoreReadOnlyException("test"));

      assertThat(errorStream.toString()).contains("Fehler: Ein Datensatz konnte nicht gespeichert werden, da der entsprechende Speicher als schreibgeschützt markiert wurde. Weitere Hinweise finden sich im Log.");
    }

    @Test
    void shouldUseFallbackExceptionMessageForExceptionWithContextWithoutLocalizedCodeNode() throws IOException {
      when(i18nCollector.findJson("en"))
        .thenReturn(Optional.of(new ObjectMapper().readTree(PLUGIN_BUNDLE_EN)));
      when(i18nCollector.findJson("de"))
        .thenReturn(Optional.of(new ObjectMapper().readTree(PLUGIN_BUNDLE_DE)));

      callExecHandler(NotFoundException.notFound(new ContextEntry.ContextBuilder()));

      assertThat(errorStream.toString()).contains("Fehler: The requested entity could not be found. It may have been deleted in another session.");
    }

    @Test
    void shouldUseParamExceptionHandlerWithTranslations() {
      CommandLine.Model.ArgSpec argSpec = mock(CommandLine.Model.ArgSpec.class);
      when(argSpec.paramLabel()).thenReturn("<name>");

      callParamHandler(new CommandLine.OverwrittenOptionException(commandLine, argSpec, "Option overwritten"), "<name>");

      assertThat(errorStream.toString())
        .contains("FEHLER: Option wurde mehrfach gesetzt: <name>");
    }
  }

  private int callExecHandler(Exception ex) {
    try {
      return executionExceptionHandler.handleExecutionException(ex, commandLine, null);
    } finally {
      stdErr.flush();
    }
  }

  private void callParamHandler(CommandLine.ParameterException ex, String... args) {
    try {
      parameterExceptionHandler.handleParseException(ex, args);
    } finally {
      stdErr.flush();
    }
  }
}
