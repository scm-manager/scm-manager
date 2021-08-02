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

package sonia.scm.search;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NonNaturalLanguageAnalyzerTest {

  private final NonNaturalLanguageAnalyzer analyzer = new NonNaturalLanguageAnalyzer();

  @ParameterizedTest
  @ValueSource(strings = {
    "simple text", "simple.text", "simple-text", "simple_text",
    "simple(text)", "simple[text]", "simpleText", "simple{text}",
    "simple:text", "simple/text", "simple;text", "simple$text",
    "simple\\text", "SimpleText"
  })
  void shouldTokenize(String value) throws IOException {
    List<String> tokens = tokenize(value);

    assertThat(tokens).containsOnly("simple", "text");
  }

  @Test
  void shouldSplitOnNumeric() throws IOException {
    List<String> tokens = tokenize("simple42text");

    assertThat(tokens).containsOnly("simple", "42", "text");
  }

  private List<String> tokenize(String text) throws IOException {
    return Analyzers.tokenize(analyzer, text);
  }


}
