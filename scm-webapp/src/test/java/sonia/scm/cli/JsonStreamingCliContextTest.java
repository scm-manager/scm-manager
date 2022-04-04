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

    try (JsonStreamingCliContext jsonStreamingCliContext = new JsonStreamingCliContext(Locale.ENGLISH, "scm-cli/1.0.0 (a1b2c3d; 2022-04-04T12:33:13Z)", bais, baos)) {
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

    try (JsonStreamingCliContext jsonStreamingCliContext = new JsonStreamingCliContext(Locale.ENGLISH, "scm-cli/1.0.0 (a1b2c3d; 2022-04-04T12:33:13Z)", bais, baos)) {
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

    try (JsonStreamingCliContext jsonStreamingCliContext = new JsonStreamingCliContext(Locale.ENGLISH, "scm-cli/1.0.0 (a1b2c3d; 2022-04-04T12:33:13Z)", bais, baos)) {
      jsonStreamingCliContext.getStdout().print("Hello");
      jsonStreamingCliContext.writeExit(1);
    }

    JsonNode json = mapper.readTree(new ByteArrayInputStream(baos.toByteArray()));
    assertThat(json.isArray()).isTrue();
    assertThat(json.get(0).get("out").asText()).isEqualTo("Hello");
    assertThat(json.get(1).get("exit").asInt()).isEqualTo(1);
  }
}
