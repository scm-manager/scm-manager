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

package sonia.scm.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class NonNaturalLanguageAnalyzerTest {

  private final NonNaturalLanguageAnalyzer analyzer = new NonNaturalLanguageAnalyzer();

  @ParameterizedTest
  @ValueSource(strings = {
    "simple text", "simple-text", "simple(text)", "simple[text]",
    "simple{text}", "simple/text", "simple;text", "simple$text",
    "simple\\text"
  })
  void shouldTokenize(String value) throws IOException {
    List<String> tokens = tokenize(value);

    assertThat(tokens).containsOnly("simple", "text");
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "simple.text", "simple_text", "simpleText", "simple:text", "SimpleText"
  })
  void shouldTokenizeAndPreserveOriginal(String value) throws IOException {
    List<String> tokens = tokenize(value);

    assertThat(tokens).containsOnly("simple", "text", value.toLowerCase(Locale.ENGLISH));
  }

  @Test
  void shouldSplitOnNumeric() throws IOException {
    List<String> tokens = tokenize("simple42text");

    assertThat(tokens).containsOnly("simple", "42", "text", "simple42text");
  }

  private List<String> tokenize(String text) throws IOException {
    return Analyzers.tokenize(analyzer, text);
  }


}
