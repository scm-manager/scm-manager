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

import com.google.common.io.Resources;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class LuceneHighlighterTest {

  @Test
  void shouldHighlightText() throws InvalidTokenOffsetsException, IOException {
    StandardAnalyzer analyzer = new StandardAnalyzer();

    Query query = new TermQuery(new Term("content", "golgafrinchan"));

    String content = content("content");

    LuceneHighlighter highlighter = new LuceneHighlighter(analyzer, query);
    String[] snippets = highlighter.highlight("content", Indexed.Analyzer.DEFAULT, content);

    assertThat(snippets).hasSize(1).allSatisfy(
      snippet -> assertThat(snippet).contains("<>Golgafrinchan</>")
    );
  }

  @Test
  void shouldHighlightCodeAndKeepLines() throws IOException, InvalidTokenOffsetsException {
    NonNaturalLanguageAnalyzer analyzer = new NonNaturalLanguageAnalyzer();
    Query query = new TermQuery(new Term("content", "die"));

    String content = content("GameOfLife.java");

    LuceneHighlighter highlighter = new LuceneHighlighter(analyzer, query);
    String[] snippets = highlighter.highlight("content", Indexed.Analyzer.CODE, content);

    assertThat(snippets).hasSize(1).allSatisfy(
      snippet -> assertThat(snippet.split("\n")).contains(
        "\t\t\t\tint neighbors= getNeighbors(above, same, below);",
        "\t\t\t\tif(neighbors < 2 || neighbors > 3){",
        "\t\t\t\t\tnewGen[row]+= \"_\";//<2 or >3 neighbors -> <>die</>",
        "\t\t\t\t}else if(neighbors == 3){",
        "\t\t\t\t\tnewGen[row]+= \"#\";//3 neighbors -> spawn/live"
      )
    );
  }

  @Test
  void shouldHighlightCodeInTsx() throws IOException, InvalidTokenOffsetsException {
    NonNaturalLanguageAnalyzer analyzer = new NonNaturalLanguageAnalyzer();
    Query query = new TermQuery(new Term("content", "inherit"));

    String content = content("Button.tsx");

    LuceneHighlighter highlighter = new LuceneHighlighter(analyzer, query);
    String[] snippets = highlighter.highlight("content", Indexed.Analyzer.CODE, content);

    assertThat(snippets).hasSize(1).allSatisfy(
      snippet -> assertThat(snippet.split("\n")).contains(
        "}) => {",
        "  const renderIcon = () => {",
        "    return <>{icon ? <Icon name={icon} color=\"<>inherit</>\" className=\"is-medium pr-1\" /> : null}</>;",
        "  };"
      )
    );
  }

  @SuppressWarnings("UnstableApiUsage")
  private String content(String resource) throws IOException {
    URL url = Resources.getResource("sonia/scm/search/" + resource + ".txt");
    return Resources.toString(url, StandardCharsets.UTF_8);
  }

}
