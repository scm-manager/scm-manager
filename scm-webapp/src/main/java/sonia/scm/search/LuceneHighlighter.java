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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.*;

import java.io.IOException;
import java.util.Arrays;

public final class LuceneHighlighter {

  private static final String PRE_TAG = "<|[[--";
  private static final String POST_TAG = "--]]|>";

  private static final int MAX_NUM_FRAGMENTS = 5;
  private static final int FRAGMENT_SIZE = 200;

  private final Analyzer analyzer;
  private final Highlighter highlighter;

  public LuceneHighlighter(Analyzer analyzer, Query query) {
    this.analyzer = analyzer;
    QueryScorer scorer = new QueryScorer(query);
    this.highlighter = new Highlighter(new SimpleHTMLFormatter(PRE_TAG, POST_TAG), scorer);
    this.highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer, FRAGMENT_SIZE));
  }

  public String[] highlight(String fieldName, Indexed.Analyzer fieldAnalyzer, String value) throws InvalidTokenOffsetsException, IOException {
    String[] fragments = highlighter.getBestFragments(analyzer, fieldName, value, MAX_NUM_FRAGMENTS);
    if (fieldAnalyzer == Indexed.Analyzer.CODE) {
      fragments = keepWholeLine(value, fragments);
    }
    return Arrays.stream(fragments)
      .map(fragment -> fragment.replace(PRE_TAG, "<>").replace(POST_TAG, "</>"))
      .toArray(String[]::new);
  }

  private String[] keepWholeLine(String content, String[] fragments) {
    return Arrays.stream(fragments)
      .map(fragment -> keepWholeLine(content, fragment))
      .toArray(String[]::new);
  }

  private String keepWholeLine(String content, String fragment) {
    String raw = fragment.replace(PRE_TAG, "").replace(POST_TAG, "");
    int index = content.indexOf(raw);

    int c = index;
    while (c > 0) {
      c--;
      if (content.charAt(c) == '\n') {
        break;
      }
    }

    String snippet = content.substring(c, index) + fragment;

    int end = content.indexOf('\n', index + raw.length());
    if (end < 0) {
      // reached end
      end = content.length();
    }

    return snippet + content.substring(index + raw.length(), end) + "\n";
  }

}
