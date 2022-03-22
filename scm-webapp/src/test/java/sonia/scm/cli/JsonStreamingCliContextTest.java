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

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JsonStreamingCliContextTest {

  @Test
  void shouldPrintJsonOnStdout() throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonStreamingCliContext jsonStreamingCliContext = new JsonStreamingCliContext(bais, baos);

    jsonStreamingCliContext.getStdout().print("Hello");

    assertThat(baos.toString(StandardCharsets.UTF_8).split("\n")).containsOnly("{\"out\":\"Hello\"}");
  }

  @Test
  void shouldPrintJsonOnStdoutAndStderr() {
    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonStreamingCliContext jsonStreamingCliContext = new JsonStreamingCliContext(bais, baos);

    jsonStreamingCliContext.getStdout().print("Hello");
    jsonStreamingCliContext.getStderr().print("Error 1: Failed");
    jsonStreamingCliContext.getStdout().print(" World");

    assertThat(baos.toString(StandardCharsets.UTF_8).split("\n"))
      .containsOnly(
        "{\"out\":\"Hello\"}",
        "{\"err\":\"Error 1: Failed\"}",
        "{\"out\":\" World\"}"
      );
  }

}
