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

import lombok.Getter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzerFactoryTest {

  private final AnalyzerFactory analyzerFactory = new AnalyzerFactory();

  @Nested
  class FromSearchableTypeTests {

    @Test
    void shouldUseDefaultAnalyzerIfNotSpecified() throws IOException {
      analyze(Account.class, "simple_text", "description", "simple_text");
    }

    @Test
    void shouldUseNonNaturalLanguageAnalyzer() throws IOException {
      analyze(Account.class, "simple_text", "username", "simple", "text", "simple_text");
    }

    private void analyze(Class<?> type, String text, String field, String... expectedTokens) throws IOException {
      LuceneSearchableType searchableType = SearchableTypes.create(type);
      Analyzer analyzer = analyzerFactory.create(searchableType);
      List<String> tokens = Analyzers.tokenize(analyzer, field, text);
      assertThat(tokens).containsOnly(expectedTokens);
    }

  }

  @Getter
  @IndexedType
  public static class Account {

    @Indexed(analyzer = Indexed.Analyzer.IDENTIFIER)
    private String username;

    @Indexed
    private String description;

  }

}
