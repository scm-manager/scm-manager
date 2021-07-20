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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzerFactoryTest {

  private final AnalyzerFactory analyzerFactory = new AnalyzerFactory();

  @Test
  void shouldReturnStandardAnalyzer() {
    Analyzer analyzer = analyzerFactory.create(IndexOptions.defaults());
    assertThat(analyzer).isInstanceOf(StandardAnalyzer.class);
  }

  @Test
  void shouldReturnStandardAnalyzerForUnknownLocale() {
    Analyzer analyzer = analyzerFactory.create(IndexOptions.naturalLanguage(Locale.CHINESE));
    assertThat(analyzer).isInstanceOf(StandardAnalyzer.class);
  }

  @Test
  void shouldReturnEnglishAnalyzer() {
    Analyzer analyzer = analyzerFactory.create(IndexOptions.naturalLanguage(Locale.ENGLISH));
    assertThat(analyzer).isInstanceOf(EnglishAnalyzer.class);
  }

  @Test
  void shouldReturnGermanAnalyzer() {
    Analyzer analyzer = analyzerFactory.create(IndexOptions.naturalLanguage(Locale.GERMAN));
    assertThat(analyzer).isInstanceOf(GermanAnalyzer.class);
  }

  @Test
  void shouldReturnGermanAnalyzerForLocaleGermany() {
    Analyzer analyzer = analyzerFactory.create(IndexOptions.naturalLanguage(Locale.GERMANY));
    assertThat(analyzer).isInstanceOf(GermanAnalyzer.class);
  }

  @Test
  void shouldReturnSpanishAnalyzer() {
    Analyzer analyzer = analyzerFactory.create(IndexOptions.naturalLanguage(new Locale("es", "ES")));
    assertThat(analyzer).isInstanceOf(SpanishAnalyzer.class);
  }

}
