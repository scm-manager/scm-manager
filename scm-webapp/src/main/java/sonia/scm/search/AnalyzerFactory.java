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
