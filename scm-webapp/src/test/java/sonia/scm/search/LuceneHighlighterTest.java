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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LuceneHighlighterTest {

  @Test
  void shouldHighlightText() throws InvalidTokenOffsetsException, IOException {
    StandardAnalyzer analyzer = new StandardAnalyzer();

    Query query = new TermQuery(new Term("content", "golgafrinchan"));

    String content = content("content");

    LuceneHighlighter highlighter = new LuceneHighlighter(analyzer, query);
    ContentFragment[] contentFragments = highlighter.highlight("content", Indexed.Analyzer.DEFAULT, content);

    assertThat(contentFragments).hasSize(1).allSatisfy(
      contentFragment -> assertThat(contentFragment.getFragment()).contains("<|[[--Golgafrinchan--]]|>")
    );
  }

  @Test
  void shouldHighlightCodeAndKeepLines() throws IOException, InvalidTokenOffsetsException {
    ContentFragment[] contentFragments = highlightCode("GameOfLife.java", "die");

    assertThat(contentFragments).hasSize(1).allSatisfy(
      contentFragment -> {
        assertThat(contentFragment.getFragment().split("\n")).contains(
          "\t\t\t\tint neighbors= getNeighbors(above, same, below);",
          "\t\t\t\tif(neighbors < 2 || neighbors > 3){",
          "\t\t\t\t\tnewGen[row]+= \"_\";//<2 or >3 neighbors -> <|[[--die--]]|>",
          "\t\t\t\t}else if(neighbors == 3){",
          "\t\t\t\t\tnewGen[row]+= \"#\";//3 neighbors -> spawn/live"
        );
        assertThat(contentFragment.isMatchesContentEnd()).isFalse();
        assertThat(contentFragment.isMatchesContentEnd()).isFalse();
      }
    );
  }

  @Test
  void shouldNotStartHighlightedFragmentWithLineBreak() throws IOException, InvalidTokenOffsetsException {
    ContentFragment[] contentFragments = highlightCode("GameOfLife.java", "die");

    assertThat(contentFragments).hasSize(1).allSatisfy(
      contentFragment -> assertThat(contentFragment.getFragment()).doesNotStartWith("\n")
    );
  }

  @Test
  void shouldHighlightCodeInTsx() throws IOException, InvalidTokenOffsetsException {
    ContentFragment[] contentFragments = highlightCode("Button.tsx", "inherit");

    assertThat(contentFragments).hasSize(1).allSatisfy(
      contentFragment -> assertThat(contentFragment.getFragment().split("\n")).contains(
        "}) => {",
        "  const renderIcon = () => {",
        "    return <>{icon ? <Icon name={icon} color=\"<|[[--inherit--]]|>\" className=\"is-medium pr-1\" /> : null}</>;",
        "  };"
      )
    );
  }

  @Test
  void shouldHighlightFirstCodeLine() throws InvalidTokenOffsetsException, IOException {
    ContentFragment[] contentFragments = highlightCode("GameOfLife.java", "gameoflife");

    assertThat(contentFragments).hasSize(1);
    assertThat(contentFragments[0].isMatchesContentStart()).isTrue();
    assertThat(contentFragments[0].isMatchesContentEnd()).isFalse();
  }

  @Test
  void shouldHighlightLastCodeLine() throws InvalidTokenOffsetsException, IOException {
    ContentFragment[] contentFragments = highlightCode("Button.tsx", "default");

    assertThat(contentFragments).hasSize(1);
    assertThat(contentFragments[0].isMatchesContentStart()).isFalse();
    assertThat(contentFragments[0].isMatchesContentEnd()).isTrue();
  }

  @Test
  void shouldMatchContentStartWithDefaultAnalyzer() throws InvalidTokenOffsetsException, IOException {
    ContentFragment[] contentFragments = highlight("GameOfLife.java", "gameoflife");

    assertThat(contentFragments).hasSize(1);
    assertThat(contentFragments[0].isMatchesContentStart()).isTrue();
    assertThat(contentFragments[0].isMatchesContentEnd()).isFalse();
  }

  @Test
  void shouldMatchContentEndWithDefaultAnalyzer() throws InvalidTokenOffsetsException, IOException {
    ContentFragment[] contentFragments = highlight("Button.tsx", "default");

    assertThat(contentFragments).hasSize(1);
    assertThat(contentFragments[0].isMatchesContentStart()).isFalse();
    assertThat(contentFragments[0].isMatchesContentEnd()).isTrue();
  }

  @Nested
  class IsHighlightableTests {

    @Mock
    private LuceneSearchableField field;

    private LuceneHighlighter highlighter;

    @BeforeEach
    void setUpHighlighter() {
      Query query = new TermQuery(new Term("content", "ka"));
      highlighter = new LuceneHighlighter(new StandardAnalyzer(), query);
    }

    @Test
    void shouldReturnFalseForNonHighlightedField() {
      when(field.isHighlighted()).thenReturn(false);

      assertThat(highlighter.isHighlightable(field)).isFalse();
    }

    @Test
    void shouldReturnFalseIfNotInQuery() {
      when(field.isHighlighted()).thenReturn(true);
      when(field.getName()).thenReturn("name");

      assertThat(highlighter.isHighlightable(field)).isFalse();
    }

    @Test
    void shouldReturnTrue() {
      when(field.isHighlighted()).thenReturn(true);
      when(field.getName()).thenReturn("content");

      assertThat(highlighter.isHighlightable(field)).isTrue();
    }

  }

  private ContentFragment[] highlight(String resource, String search) throws IOException, InvalidTokenOffsetsException {
    StandardAnalyzer analyzer = new StandardAnalyzer();
    Query query = new TermQuery(new Term("content", search));

    String content = content(resource);

    LuceneHighlighter highlighter = new LuceneHighlighter(analyzer, query);
    return highlighter.highlight("content", Indexed.Analyzer.DEFAULT, content);
  }

  private ContentFragment[] highlightCode(String resource, String search) throws IOException, InvalidTokenOffsetsException {
    NonNaturalLanguageAnalyzer analyzer = new NonNaturalLanguageAnalyzer();
    Query query = new TermQuery(new Term("content", search));

    String content = content(resource);

    LuceneHighlighter highlighter = new LuceneHighlighter(analyzer, query);
    return highlighter.highlight("content", Indexed.Analyzer.CODE, content);
  }

  @SuppressWarnings("UnstableApiUsage")
  private String content(String resource) throws IOException {
    URL url = Resources.getResource("sonia/scm/search/" + resource + ".txt");
    return Resources.toString(url, StandardCharsets.UTF_8);
  }

}
