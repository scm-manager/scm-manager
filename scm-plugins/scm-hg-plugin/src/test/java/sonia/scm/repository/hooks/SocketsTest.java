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

package sonia.scm.repository.hooks;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.generate;
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
    assertThrows(EOFException.class, () -> Sockets.receive(input, TestValue.class));
  }

  @Test
  void shouldFailWithTooFewBytesForData() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    output.write((16 >>> 24) & 0xFF);
    output.write((16 >>> 16) & 0xFF);
    output.write((16 >>>  8) & 0xFF);
    output.write(16 & 0xFF);

    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    assertThrows(EOFException.class, () -> Sockets.receive(input, TestValue.class));
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

  @Test
  void shouldSendAndReceiveWithChunks() throws IOException {
    String stringValue = generate(() -> "a").limit(1024).collect(joining());

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    Sockets.send(output, new TestValue(stringValue));
    InputStream input = new ByteArrayInputStream(output.toByteArray()) {
      @Override
      public synchronized int read(byte[] b, int off, int len) {
        return super.read(b, off, Math.min(8, len));
      }
    };
    TestValue value = Sockets.receive(input, TestValue.class);
    assertThat(value.value).hasSize(1024);
  }

  @Data
  @AllArgsConstructor
  public static class TestValue {

    private String value;

  }

}
