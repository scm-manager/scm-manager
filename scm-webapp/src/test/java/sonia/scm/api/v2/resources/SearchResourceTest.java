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

package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.JsonNode;
import de.otto.edison.hal.HalRepresentation;
import lombok.Getter;
import lombok.Setter;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.search.Hit;
import sonia.scm.search.IndexNames;
import sonia.scm.search.QueryResult;
import sonia.scm.search.SearchEngine;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchResourceTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SearchEngine searchEngine;

  private RestDispatcher dispatcher;

  @Mock
  private HalEnricherRegistry enricherRegistry;

  @BeforeEach
  void setUpDispatcher() {
    QueryResultMapper mapper = Mappers.getMapper(QueryResultMapper.class);
    mapper.setRegistry(enricherRegistry);
    SearchResource resource = new SearchResource(
      searchEngine, mapper
    );
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }

  @Test
  void shouldEnrichQueryResult() throws IOException, URISyntaxException {
    when(enricherRegistry.allByType(QueryResult.class))
      .thenReturn(Collections.singleton(new SampleEnricher()));

    mockQueryResult("Hello", result(0L));
    JsonMockHttpResponse response = search("Hello");

    JsonNode sample = response.getContentAsJson().get("_embedded").get("sample");
    assertThat(sample.get("type").asText()).isEqualTo("java.lang.String");
  }

  @Test
  void shouldEnrichHitResult() throws IOException, URISyntaxException {
    when(enricherRegistry.allByType(QueryResult.class))
      .thenReturn(Collections.emptySet());
    when(enricherRegistry.allByType(Hit.class))
      .thenReturn(Collections.singleton(new SampleEnricher()));

    mockQueryResult("Hello", result(1L, "Hello"));
    JsonMockHttpResponse response = search("Hello");

    JsonNode sample = response.getContentAsJson()
      .get("_embedded").get("hits").get(0)
      .get("_embedded").get("sample");
    assertThat(sample.get("type").asText()).isEqualTo("java.lang.String");
  }

  @Nested
  class WithoutEnricher {

    @BeforeEach
    void setUpEnricherRegistry() {
      when(enricherRegistry.allByType(QueryResult.class)).thenReturn(Collections.emptySet());
      lenient().when(enricherRegistry.allByType(Hit.class)).thenReturn(Collections.emptySet());
    }

    @Test
    void shouldReturnVndContentType() throws UnsupportedEncodingException, URISyntaxException {
      mockQueryResult("Hello", result(0L));
      JsonMockHttpResponse response = search("Hello");
      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      assertHeader(response, "Content-Type", VndMediaType.QUERY_RESULT);
    }

    @Test
    void shouldReturnPagingLinks() throws IOException, URISyntaxException {
      mockQueryResult(20, 20, "paging", result(100));
      JsonMockHttpResponse response = search("paging", 1, 20);

      JsonNode links = response.getContentAsJson().get("_links");
      assertLink(links, "self", "/v2/search/query/string?q=paging&page=1&pageSize=20");
      assertLink(links, "first", "/v2/search/query/string?q=paging&page=0&pageSize=20");
      assertLink(links, "prev", "/v2/search/query/string?q=paging&page=0&pageSize=20");
      assertLink(links, "next", "/v2/search/query/string?q=paging&page=2&pageSize=20");
      assertLink(links, "last", "/v2/search/query/string?q=paging&page=4&pageSize=20");
    }

    @Test
    void shouldPagingFields() throws IOException, URISyntaxException {
      mockQueryResult(20, 20, "pagingFields", result(100));
      JsonMockHttpResponse response = search("pagingFields", 1, 20);

      JsonNode root = response.getContentAsJson();
      assertThat(root.get("page").asInt()).isOne();
      assertThat(root.get("pageTotal").asInt()).isEqualTo(5);
    }

    @Test
    void shouldReturnType() throws IOException, URISyntaxException {
      mockQueryResult("Hello", result(0L));
      JsonMockHttpResponse response = search("Hello");

      JsonNode root = response.getContentAsJson();
      assertThat(root.get("type").asText()).isEqualTo("java.lang.String");
    }

    @Test
    void shouldReturnHitsAsEmbedded() throws IOException, URISyntaxException {
      mockQueryResult("Hello", result(2L, "Hello", "Hello Again"));
      JsonMockHttpResponse response = search("Hello");

      JsonNode hits = response.getContentAsJson().get("_embedded").get("hits");
      assertThat(hits.size()).isEqualTo(2);

      JsonNode first = hits.get(0);
      assertThat(first.get("score").asDouble()).isEqualTo(2d);

      JsonNode fields = first.get("fields");

      JsonNode valueField = fields.get("value");
      assertThat(valueField.get("highlighted").asBoolean()).isFalse();
      assertThat(valueField.get("value").asText()).isEqualTo("Hello");

      JsonNode highlightedField = fields.get("highlighted");
      assertThat(highlightedField.get("highlighted").asBoolean()).isTrue();
      assertThat(highlightedField.get("fragments").get(0).asText()).isEqualTo("Hello");
    }

  }

  private void assertLink(JsonNode links, String self, String s) {
    assertThat(links.get(self).get("href").asText()).isEqualTo(s);
  }

  private QueryResult result(long totalHits, Object... values) {
    List<Hit> hits = new ArrayList<>();
    for (int i = 0; i < values.length; i++) {
      hits.add(hit(i, values));
    }
    return new QueryResult(totalHits, String.class, hits);
  }

  @Nonnull
  private Hit hit(int i, Object[] values) {
    Map<String, Hit.Field> fields = fields(values[i]);
    return new Hit("" + i, values.length - i, fields);
  }

  @Nonnull
  private Map<String, Hit.Field> fields(Object value) {
    Map<String, Hit.Field> fields = new HashMap<>();
    fields.put("value", new Hit.ValueField(value));
    fields.put("highlighted", new Hit.HighlightedField(new String[]{value.toString()}));
    return fields;
  }

  private void mockQueryResult(String query, QueryResult result) {
    mockQueryResult(0, 10, query, result);
  }

  private void mockQueryResult(int start, int limit, String query, QueryResult result) {
    when(
      searchEngine.search(IndexNames.DEFAULT)
        .start(start)
        .limit(limit)
        .execute("string", query)
    ).thenReturn(result);
  }

  private void assertHeader(JsonMockHttpResponse response, String header, String expectedValue) {
    assertThat(response.getOutputHeaders().getFirst(header)).hasToString(expectedValue);
  }

  private JsonMockHttpResponse search(String query) throws URISyntaxException, UnsupportedEncodingException {
    return search(query, null, null);
  }

  private JsonMockHttpResponse search(String query, Integer page, Integer pageSize) throws URISyntaxException, UnsupportedEncodingException {
    String uri = "/v2/search/query/string?q=" + URLEncoder.encode(query, "UTF-8");
    if (page != null) {
      uri += "&page=" + page;
    }
    if (pageSize != null) {
      uri += "&pageSize=" + pageSize;
    }
    MockHttpRequest request = MockHttpRequest.get(uri);
    JsonMockHttpResponse response = new JsonMockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  @Getter
  @Setter
  public static class SampleEmbedded extends HalRepresentation {
    private Class<?> type;
  }

  private static class SampleEnricher implements HalEnricher {
    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {
      QueryResult result = context.oneRequireByType(QueryResult.class);

      SampleEmbedded embedded = new SampleEmbedded();
      embedded.setType(result.getType());

      appender.appendEmbedded("sample", embedded);
    }
  }
}
