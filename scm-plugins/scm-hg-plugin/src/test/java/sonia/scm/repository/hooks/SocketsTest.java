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

package sonia.scm.repository.hooks;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SocketsTest {

  @Test
  void shouldSendAndReceive() throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    Sockets.send(output, new TestValue("awesome"));
    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    TestValue value = Sockets.receive(input, TestValue.class);
    assertThat(value.value).isEqualTo("awesome");
  }

  @Test
  void shouldFailWithTooFewBytesForLength() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    output.write((512 >>> 24) & 0xFF);
    output.write((512 >>> 16) & 0xFF);

    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    IOException ex = assertThrows(IOException.class, () -> Sockets.receive(input, TestValue.class));
    assertThat(ex.getMessage()).contains("int");
  }

  @Test
  void shouldFailWithTooFewBytesForData() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    output.write((16 >>> 24) & 0xFF);
    output.write((16 >>> 16) & 0xFF);
    output.write((16 >>>  8) & 0xFF);
    output.write(16 & 0xFF);

    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    IOException ex = assertThrows(IOException.class, () -> Sockets.receive(input, TestValue.class));
    assertThat(ex.getMessage()).contains("bytes");
  }

  @Test
  void shouldFailIfLimitIsExceeded() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    output.write((9216 >>> 24) & 0xFF);
    output.write((9216 >>> 16) & 0xFF);
    output.write((9216 >>>  8) & 0xFF);
    output.write(9216 & 0xFF);

    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    IOException ex = assertThrows(IOException.class, () -> Sockets.receive(input, TestValue.class));
    assertThat(ex.getMessage()).contains("9216");
  }

  @Data
  @AllArgsConstructor
  public static class TestValue {

    private String value;

  }

}
