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
        "    return <>{icon ? <Icon name={icon} color=\"<|[[--inherit--]]|>\" className=\"is-medium pr-1\" /> : null}</>;"
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
