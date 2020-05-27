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

package sonia.scm.api.v2.resources;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class LineFilteredOutputStreamTest {

  static final String INPUT_LF = "line 1\nline 2\nline 3\nline 4";
  static final String INPUT_CR_LF = "line 1\r\nline 2\r\nline 3\r\nline 4";
  static final String INPUT_CR = "line 1\rline 2\rline 3\rline 4";

  ByteArrayOutputStream target = new ByteArrayOutputStream();

  @ParameterizedTest
  @ValueSource(strings = {INPUT_LF, INPUT_CR_LF, INPUT_CR})
  void shouldNotFilterIfStartAndEndAreNotSet(String input) throws IOException {
    try (LineFilteredOutputStream filtered = new LineFilteredOutputStream(target, null, null)) {
      filtered.write(input.getBytes());
    }

    assertThat(target.toString()).isEqualTo(INPUT_LF);
  }

  @ParameterizedTest
  @ValueSource(strings = {INPUT_LF, INPUT_CR_LF, INPUT_CR})
  void shouldNotFilterIfStartAndEndAreSetToLimits(String input) throws IOException {
    try (LineFilteredOutputStream filtered = new LineFilteredOutputStream(target, 0, 4)) {
      filtered.write(input.getBytes());
    }

    assertThat(target.toString()).isEqualTo(INPUT_LF);
  }

  @ParameterizedTest
  @ValueSource(strings = {INPUT_LF, INPUT_CR_LF, INPUT_CR})
  void shouldRemoveFirstLinesIfStartIsSetGreaterThat1(String input) throws IOException {
    LineFilteredOutputStream filtered = new LineFilteredOutputStream(target, 2, null);

    filtered.write(input.getBytes());

    assertThat(target.toString()).isEqualTo("line 3\nline 4");
  }

  @ParameterizedTest
  @ValueSource(strings = {INPUT_LF, INPUT_CR_LF, INPUT_CR})
  void shouldOmitLastLinesIfEndIsSetLessThatLength(String input) throws IOException {
    LineFilteredOutputStream filtered = new LineFilteredOutputStream(target, null, 2);

    filtered.write(input.getBytes());

    assertThat(target.toString()).isEqualTo("line 1\nline 2\n");
  }
}
