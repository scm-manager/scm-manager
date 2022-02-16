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
import org.apache.lucene.search.QueryVisitor;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class LuceneHighlighter {

  private static final String PRE_TAG = "<|[[--";
  private static final String POST_TAG = "--]]|>";

  private static final int MAX_NUM_FRAGMENTS = 5;
  private static final int FRAGMENT_SIZE = 200;

  private final Analyzer analyzer;
  private final Highlighter highlighter;

  private final Set<String> queriedFields = new HashSet<>();

  public LuceneHighlighter(Analyzer analyzer, Query query) {
    this.analyzer = analyzer;
    QueryScorer scorer = new QueryScorer(query);
    this.highlighter = new Highlighter(new SimpleHTMLFormatter(PRE_TAG, POST_TAG), scorer);
    this.highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer, FRAGMENT_SIZE));

    query.visit(new QueryVisitor() {
      @Override
      public boolean acceptField(String field) {
        queriedFields.add(field);
        return super.acceptField(field);
      }
    });
  }

  public boolean isHighlightable(LuceneSearchableField field) {
    return field.isHighlighted() && queriedFields.contains(field.getName());
  }

  public ContentFragment[] highlight(String fieldName, Indexed.Analyzer fieldAnalyzer, String value) throws InvalidTokenOffsetsException, IOException {
    String[] fragments = highlighter.getBestFragments(analyzer, fieldName, value, MAX_NUM_FRAGMENTS);
    if (fieldAnalyzer == Indexed.Analyzer.CODE) {
      return keepWholeLine(value, fragments);
    }
    return Arrays.stream(fragments).map(f -> createContentFragment(value, f))
      .toArray(ContentFragment[]::new);
  }

  private ContentFragment[] keepWholeLine(String content, String[] fragments) {
    return Arrays.stream(fragments)
      .map(fragment -> keepWholeLine(content, fragment))
      .toArray(ContentFragment[]::new);
  }

  private ContentFragment keepWholeLine(String content, String fragment) {
    boolean matchesContentStart = false;
    boolean matchesContentEnd = false;

    String raw = fragment.replace(PRE_TAG, "").replace(POST_TAG, "");
    int index = content.indexOf(raw);

    if (index == 0) {
      matchesContentStart = true;
    }

    int start = content.lastIndexOf('\n', index);

    String snippet;
    if (start == index) {
      // fragment starts with a linebreak
      snippet = fragment.substring(1);
    } else {
      if (start < 0) {
        // no leading linebreak
        start = 0;
      } else if (start < content.length()) {
        // skip linebreak
        start++;
      }
      snippet = content.substring(start, index) + fragment;
    }

    int end = content.indexOf('\n', index + raw.length());
    if (end < 0) {
      end = content.length();
      matchesContentEnd = true;
    }

    return new ContentFragment(snippet + content.substring(index + raw.length(), end) + (matchesContentEnd ? "" : "\n"), matchesContentStart, matchesContentEnd);
  }

  private ContentFragment createContentFragment(String content, String fragment) {
    String raw = fragment.replace(PRE_TAG, "").replace(POST_TAG, "");
    return new ContentFragment(fragment, content.startsWith(raw), content.endsWith(raw));
  }

}
