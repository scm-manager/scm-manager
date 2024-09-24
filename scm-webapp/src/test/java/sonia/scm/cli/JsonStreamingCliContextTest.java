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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class JsonStreamingCliContextTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Test
  void shouldPrintJsonOnStdout() throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (JsonStreamingCliContext jsonStreamingCliContext = new JsonStreamingCliContext(Locale.ENGLISH,  new Client("cli", "1.0.0"), bais, baos)) {
      jsonStreamingCliContext.getStdout().print("Hello");
    }

    JsonNode json = mapper.readTree(new ByteArrayInputStream(baos.toByteArray()));
    assertThat(json.isArray()).isTrue();
    assertThat(json.get(0).get("out").asText()).isEqualTo("Hello");
  }

  @Test
  void shouldPrintJsonOnStdoutAndStderr() throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (JsonStreamingCliContext jsonStreamingCliContext = new JsonStreamingCliContext(Locale.ENGLISH, new Client("cli", "1.0.0"), bais, baos)) {
      jsonStreamingCliContext.getStdout().print("Hello");
      jsonStreamingCliContext.getStderr().print("Error 1: Failed");
      jsonStreamingCliContext.getStdout().print(" World");
    }

    JsonNode json = mapper.readTree(new ByteArrayInputStream(baos.toByteArray()));
    assertThat(json.isArray()).isTrue();
    assertThat(json.get(0).get("out").asText()).isEqualTo("Hello");
    assertThat(json.get(1).get("err").asText()).isEqualTo("Error 1: Failed");
    assertThat(json.get(2).get("out").asText()).isEqualTo(" World");
  }

  @Test
  void shouldReturnExitCode() throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (JsonStreamingCliContext jsonStreamingCliContext = new JsonStreamingCliContext(Locale.ENGLISH,  new Client("cli", "1.0.0"), bais, baos)) {
      jsonStreamingCliContext.getStdout().print("Hello");
      jsonStreamingCliContext.writeExit(1);
    }

    JsonNode json = mapper.readTree(new ByteArrayInputStream(baos.toByteArray()));
    assertThat(json.isArray()).isTrue();
    assertThat(json.get(0).get("out").asText()).isEqualTo("Hello");
    assertThat(json.get(1).get("exit").asInt()).isEqualTo(1);
  }
}
