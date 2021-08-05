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
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class AnalyzerFactory {

  @Nonnull
  public Analyzer create(IndexOptions options) {
    if (options.getType() == IndexOptions.Type.NATURAL_LANGUAGE) {
      return createNaturalLanguageAnalyzer(options.getLocale().getLanguage());
    }
    return createDefaultAnalyzer();
  }

  private Analyzer createDefaultAnalyzer() {
    return new StandardAnalyzer();
  }

  private Analyzer createNaturalLanguageAnalyzer(String lang) {
    switch (lang) {
      case "en":
        return new EnglishAnalyzer();
      case "de":
        return new GermanAnalyzer();
      case "es":
        return new SpanishAnalyzer();
      default:
        return createDefaultAnalyzer();
    }
  }

  public Analyzer create(LuceneSearchableType type, IndexOptions options) {
    Analyzer defaultAnalyzer = create(options);

    Map<String, Analyzer> analyzerMap = new HashMap<>();
    for (LuceneSearchableField field : type.getFields()) {
      addFieldAnalyzer(analyzerMap, field);
    }

    return new PerFieldAnalyzerWrapper(defaultAnalyzer, analyzerMap);
  }

  private void addFieldAnalyzer(Map<String, Analyzer> analyzerMap, LuceneSearchableField field) {
    if (field.getAnalyzer() != Indexed.Analyzer.DEFAULT) {
      analyzerMap.put(field.getName(), new NonNaturalLanguageAnalyzer());
    }
  }

}
