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
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Analyzers {

  private Analyzers() {
  }

  static List<String> tokenize(Analyzer analyzer, String text) throws IOException {
    return tokenize(analyzer, "default", text);
  }

  static List<String> tokenize(Analyzer analyzer, String field, String text) throws IOException {
    List<String> tokens = new ArrayList<>();
    try (TokenStream stream = analyzer.tokenStream(field, text)) {
      CharTermAttribute attr = stream.addAttribute(CharTermAttribute.class);
      stream.reset();
      while (stream.incrementToken()) {
        tokens.add(attr.toString());
      }
    }
    return tokens;
  }

}
