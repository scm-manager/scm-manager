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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.UpperCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AnalyzerFactory {

  public Analyzer create(LuceneSearchableType type) {
    Analyzer defaultAnalyzer = createNonTokenizedAnalyzer();

    Map<String, Analyzer> analyzerMap = new HashMap<>();
    for (LuceneSearchableField field : type.getAllFields()) {
      addFieldAnalyzer(analyzerMap, field);
    }

    return new PerFieldAnalyzerWrapper(defaultAnalyzer, analyzerMap);
  }

  private Analyzer createNonTokenizedAnalyzer() {
    return new KeywordAnalyzer();
  }

  private void addFieldAnalyzer(Map<String, Analyzer> analyzerMap, LuceneSearchableField field) {
    Analyzer analyzer = createAnalyzer(field);
    if (analyzer != null) {
      analyzerMap.put(field.getName(), analyzer);
    }
  }

  private Analyzer createAnalyzer(LuceneSearchableField field) {
    if (field.isTokenized()) {
      return createTokenizedAnalyzer(field.getAnalyzer());
    } else if (field.getType().isEnum()) {
      return createEnumAnalyzer();
    } else {
      return null;
    }
  }

  private Analyzer createTokenizedAnalyzer(Indexed.Analyzer analyzer) {
    if (analyzer == Indexed.Analyzer.DEFAULT) {
      return new StandardAnalyzer();
    }
    return new NonNaturalLanguageAnalyzer();
  }

  private Analyzer createEnumAnalyzer() {
    try {
      return CustomAnalyzer.builder()
        .withTokenizer(KeywordTokenizerFactory.class)
        .addTokenFilter(UpperCaseFilterFactory.class)
        .build();
    } catch (IOException ex) {
      throw new IllegalStateException("failed to create enum analyzer", ex);
    }
  }

}
