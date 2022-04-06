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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

  private CliExceptionHandler handler;

  private final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
  private final PrintWriter stdErr = new PrintWriter(errorStream);

  @BeforeEach
  void initErrorStream() {
    when(commandLine.getErr()).thenReturn(stdErr);
  }

  @Nested
  class EnglishLanguageTest {

    @BeforeEach
    void setUpHandler() {
      handler = new CliExceptionHandler(i18nCollector, "en");
    }

    @Test
    void shouldPrintSimpleException() {
      int exitCode = callHandler(new NullPointerException("expected error"));

      assertThat(errorStream.toString()).contains("Error: expected error");
      assertThat(exitCode).isEqualTo(1);
    }

    @Test
    void shouldPrintTransactionIdIfPresent() {
      TransactionId.set("42");

      callHandler(new NullPointerException("expected error"));

      assertThat(errorStream.toString()).contains("Transaction ID: 42");
    }

    @Test
    void shouldUseTranslatedMessageFromExceptionWithContext() throws IOException {
      when(i18nCollector.findJson("en"))
        .thenReturn(Optional.of(new ObjectMapper().readTree(PLUGIN_BUNDLE_EN)));

      callHandler(NotFoundException.notFound(new ContextEntry.ContextBuilder()));

      assertThat(errorStream.toString()).contains("Error: The requested entity could not be found. It may have been deleted in another session.");
    }

    @Test
    void shouldUseExceptionMessageForExceptionWithContextWithoutCodeNode() throws IOException {
      when(i18nCollector.findJson("en"))
        .thenReturn(Optional.of(new ObjectMapper().readTree(PLUGIN_BUNDLE_EN)));

      callHandler(new AlreadyExistsException(new Group("test", "hog")));

      assertThat(errorStream.toString()).contains("Error: group with id hog already exists");
    }

    @Test
    void shouldUseExceptionMessageForExceptionWithContextWithoutDescriptionNode() throws IOException {
      when(i18nCollector.findJson("en"))
        .thenReturn(Optional.of(new ObjectMapper().readTree(PLUGIN_BUNDLE_EN)));

      callHandler(new StoreReadOnlyException("test"));

      assertThat(errorStream.toString()).contains("Error: Store is read only, could not write location test");
    }
  }

  @Nested
  class GermanLanguageTest {

    @BeforeEach
    void setUpHandler() {
      handler = new CliExceptionHandler(i18nCollector, "de");
    }

    @Test
    void shouldPrintSimpleException() {
      int exitCode = callHandler(new NullPointerException("expected error"));

      assertThat(errorStream.toString()).contains("Fehler: expected error");
      assertThat(exitCode).isEqualTo(1);
    }


    @Test
    void shouldPrintTransactionIdIfPresent() {
      TransactionId.set("42");

      callHandler(new NullPointerException("expected error"));

      assertThat(errorStream.toString()).contains("Transaktions-ID: 42");
    }

    @Test
    void shouldUseTranslatedMessageFromExceptionWithContext() throws IOException {
      when(i18nCollector.findJson("de"))
        .thenReturn(Optional.of(new ObjectMapper().readTree(PLUGIN_BUNDLE_DE)));

      callHandler(new StoreReadOnlyException("test"));

      assertThat(errorStream.toString()).contains("Fehler: Ein Datensatz konnte nicht gespeichert werden, da der entsprechende Speicher als schreibgeschützt markiert wurde. Weitere Hinweise finden sich im Log.");
    }

    @Test
    void shouldUseFallbackExceptionMessageForExceptionWithContextWithoutLocalizedCodeNode() throws IOException {
      when(i18nCollector.findJson("en"))
        .thenReturn(Optional.of(new ObjectMapper().readTree(PLUGIN_BUNDLE_EN)));
      when(i18nCollector.findJson("de"))
        .thenReturn(Optional.of(new ObjectMapper().readTree(PLUGIN_BUNDLE_DE)));

      callHandler(NotFoundException.notFound(new ContextEntry.ContextBuilder()));

      assertThat(errorStream.toString()).contains("Fehler: The requested entity could not be found. It may have been deleted in another session.");
    }
  }

  private int callHandler(Exception ex) {
    try {
      return handler.handleExecutionException(ex, commandLine, null);
    } finally {
      stdErr.flush();
    }
  }
}
