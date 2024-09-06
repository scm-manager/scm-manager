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

package sonia.scm.api.v2.resources;

import org.junit.jupiter.api.Test;
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

  @ParameterizedTest
  @ValueSource(strings = {"line 1\n\nline 2\n\nline 3", "line 1\r\n\r\nline 2\r\n\r\nline 3"})
  void shouldHandleDoubleBlankLinesCorrectly(String input) throws IOException {
    LineFilteredOutputStream filtered = new LineFilteredOutputStream(target, 4, null);

    filtered.write(input.getBytes());

    assertThat(target.toString()).isEqualTo("line 3");
  }

  @ParameterizedTest
  @ValueSource(strings = {"line 1\n\n\nline 2\n\n\nline 3", "line 1\r\n\r\n\r\nline 2\r\n\r\n\r\nline 3"})
  void shouldHandleTripleBlankLinesCorrectly(String input) throws IOException {
    LineFilteredOutputStream filtered = new LineFilteredOutputStream(target, 4, 6);

    filtered.write(input.getBytes());

    assertThat(target.toString()).isEqualTo("\n\n");
  }
}
