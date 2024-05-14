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
import com.google.inject.util.Providers;
import de.otto.edison.hal.HalRepresentation;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.assertj.core.util.Lists;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCoordinates;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.search.Hit;
import sonia.scm.search.QueryBuilder;
import sonia.scm.search.QueryCountResult;
import sonia.scm.search.QueryResult;
import sonia.scm.search.QueryType;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SearchableType;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchResourceTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SearchEngine searchEngine;

  @Mock
  private RepositoryManager repositoryManager;

  private RestDispatcher dispatcher;

  @Mock
  private HalEnricherRegistry enricherRegistry;

  @BeforeEach
  void setUpDispatcher() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("/"));

    QueryResultMapper queryResultMapper = Mappers.getMapper(QueryResultMapper.class);
    queryResultMapper.setRepositoryManager(repositoryManager);
    queryResultMapper.setResourceLinks(new ResourceLinks(Providers.of(scmPathInfoStore)));

    SearchableTypeMapper searchableTypeMapper = Mappers.getMapper(SearchableTypeMapper.class);
    queryResultMapper.setRegistry(enricherRegistry);
    SearchResource resource = new SearchResource(
      searchEngine, queryResultMapper, searchableTypeMapper, repositoryManager
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
    assertThat(sample.get("value").asText()).isEqualTo("java.lang.String");
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
    assertThat(sample.get("value").asText()).isEqualTo("java.lang.String");
  }

  @Test
  void shouldEnrichRepository() throws UnsupportedEncodingException, URISyntaxException {
    when(enricherRegistry.allByType(QueryResult.class))
      .thenReturn(Collections.emptySet());
    when(enricherRegistry.allByType(Hit.class))
      .thenReturn(Collections.singleton(new SampleEnricher()));

    when(enricherRegistry.allByType(RepositoryCoordinates.class))
      .thenReturn(Collections.singleton(new RepositoryEnricher()));

    Repository heartOfGold = RepositoryTestData.createHeartOfGold("hg");
    heartOfGold.setId("42");
    when(repositoryManager.get("42")).thenReturn(heartOfGold);

    Hit hit = new Hit("21", "42", 21f, Collections.emptyMap());
    QueryResult result = new QueryResult(1L, String.class, Collections.singletonList(hit), null);

    mockQueryResult("hello", result);
    JsonMockHttpResponse response = search("hello");

    JsonNode sample = response.getContentAsJson()
      .get("_embedded").get("hits").get(0)
      .get("_embedded").get("repository")
      .get("_embedded").get("sample");

    assertThat(sample.get("value").asText()).isEqualTo("42");
  }

  private void assertLink(JsonNode links, String self, String s) {
    assertThat(links.get(self).get("href").asText()).isEqualTo(s);
  }

  private QueryResult result(long totalHits, Object... values) {
    List<Hit> hits = new ArrayList<>();
    for (int i = 0; i < values.length; i++) {
      hits.add(hit(i, values));
    }
    return new QueryResult(totalHits, String.class, hits, null);
  }

  @Nonnull
  private Hit hit(int i, Object[] values) {
    Map<String, Hit.Field> fields = fields(values[i]);
    return new Hit("" + i, null, values.length - i, fields);
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
      searchEngine.forType("string")
        .search()
        .start(start)
        .limit(limit)
        .execute(query)
    ).thenReturn(result);
  }

  private void assertHeader(JsonMockHttpResponse response, String header, String expectedValue) {
    assertThat(response.getOutputHeaders().getFirst(header))
      .asString()
      .isEqualToIgnoringCase(expectedValue);
  }

  private JsonMockHttpResponse search(String query) throws URISyntaxException, UnsupportedEncodingException {
    return search(query, null, null);
  }

  private JsonMockHttpResponse count(String query) throws URISyntaxException, UnsupportedEncodingException {
    return search(query, null, null, true);
  }

  private JsonMockHttpResponse search(String query, Integer page, Integer pageSize) throws URISyntaxException, UnsupportedEncodingException {
    return search(query, page, pageSize, false);
  }

  private JsonMockHttpResponse search(String query, Integer page, Integer pageSize, boolean countOnly) throws URISyntaxException, UnsupportedEncodingException {
    String uri = "/v2/search/query/string?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

    if (page != null) {
      uri += "&page=" + page;
    }
    if (pageSize != null) {
      uri += "&pageSize=" + pageSize;
    }
    if (countOnly) {
      uri += "&countOnly=true";
    }

    MockHttpRequest request = MockHttpRequest.get(uri);
    JsonMockHttpResponse response = new JsonMockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  @Getter
  @Setter
  public static class SampleEmbedded extends HalRepresentation {
    private String value;
  }

  private static class RepositoryEnricher implements HalEnricher {

    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {
      RepositoryCoordinates repositoryCoordinates = context.oneRequireByType(RepositoryCoordinates.class);

      SampleEmbedded embedded = new SampleEmbedded();
      embedded.setValue(repositoryCoordinates.getId());

      appender.appendEmbedded("sample", embedded);
    }
  }

  private static class SampleEnricher implements HalEnricher {
    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {
      QueryResult result = context.oneRequireByType(QueryResult.class);

      SampleEmbedded embedded = new SampleEmbedded();
      embedded.setValue(result.getType().getName());

      appender.appendEmbedded("sample", embedded);
    }
  }

  @Nested
  class SearchableTypes {

    @Mock
    private SearchableType searchableTypeOne;
    @Mock
    private SearchableType searchableTypeTwo;

    @Test
    void shouldReturnGlobalSearchableTypes() throws URISyntaxException {
      when(searchEngine.getSearchableTypes()).thenReturn(Lists.list(searchableTypeOne, searchableTypeTwo));
      when(searchableTypeOne.getName()).thenReturn("Type One");
      when(searchableTypeTwo.getName()).thenReturn("Type Two");

      MockHttpRequest request = MockHttpRequest.get("/v2/search/searchableTypes");
      JsonMockHttpResponse response = new JsonMockHttpResponse();
      dispatcher.invoke(request, response);

      JsonNode contentAsJson = response.getContentAsJson();
      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(contentAsJson.isArray()).isTrue();
      assertThat(contentAsJson.get(0).get("name").asText()).isEqualTo("Type One");
      assertThat(contentAsJson.get(1).get("name").asText()).isEqualTo("Type Two");
    }

    @Test
    void shouldReturnSearchableTypesForNamespace() throws URISyntaxException {
      when(searchEngine.getSearchableTypes()).thenReturn(Lists.list(searchableTypeOne, searchableTypeTwo));
      when(searchableTypeOne.getName()).thenReturn("Type One");
      when(searchableTypeOne.limitableToNamespace()).thenReturn(true);

      MockHttpRequest request = MockHttpRequest.get("/v2/search/searchableTypes/space");
      JsonMockHttpResponse response = new JsonMockHttpResponse();
      dispatcher.invoke(request, response);

      JsonNode contentAsJson = response.getContentAsJson();
      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(contentAsJson.isArray()).isTrue();
      assertThat(contentAsJson.get(0).get("name").asText()).isEqualTo("Type One");
      assertThat(contentAsJson.get(1)).isNull();
    }

    @Test
    void shouldReturnSearchableTypesForRepository() throws URISyntaxException {
      when(searchEngine.getSearchableTypes()).thenReturn(Lists.list(searchableTypeOne, searchableTypeTwo));
      when(searchableTypeOne.getName()).thenReturn("Type One");
      when(searchableTypeOne.limitableToRepository()).thenReturn(true);

      MockHttpRequest request = MockHttpRequest.get("/v2/search/searchableTypes/hitchhiker/hog");
      JsonMockHttpResponse response = new JsonMockHttpResponse();
      dispatcher.invoke(request, response);

      JsonNode contentAsJson = response.getContentAsJson();
      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(contentAsJson.isArray()).isTrue();
      assertThat(contentAsJson.get(0).get("name").asText()).isEqualTo("Type One");
      assertThat(contentAsJson.get(1)).isNull();
    }
  }

  @Nested
  class WithoutEnricher {

    @BeforeEach
    void setUpEnricherRegistry() {
      when(enricherRegistry.allByType(QueryResult.class)).thenReturn(Collections.emptySet());
      lenient().when(enricherRegistry.allByType(Hit.class)).thenReturn(Collections.emptySet());
      lenient().when(enricherRegistry.allByType(RepositoryCoordinates.class)).thenReturn(Collections.emptySet());
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

    @Test
    void shouldReturnCountOnly() throws URISyntaxException {
      when(
        searchEngine.forType("string")
          .search()
          .count("Hello")
      ).thenReturn(new QueryCountResult(String.class, 2L, null));

      MockHttpRequest request = MockHttpRequest.get("/v2/search/query/string?q=Hello&countOnly=true");
      JsonMockHttpResponse response = new JsonMockHttpResponse();
      dispatcher.invoke(request, response);

      JsonNode node = response.getContentAsJson();

      assertThat(node.get("totalHits").asLong()).isEqualTo(2L);

      JsonNode hits = node.get("_embedded").get("hits");
      assertThat(hits.size()).isZero();
    }

    @Test
    void shouldReturnSimpleQueryTypeForSearch() throws URISyntaxException, UnsupportedEncodingException {
      mockQueryResult(
        "hello",
        new QueryResult(0L, String.class, List.of(), QueryType.SIMPLE)
      );

      JsonMockHttpResponse response = search("hello");

      JsonNode node = response.getContentAsJson();
      assertThat(node.get("queryType").asText()).isEqualTo(QueryType.SIMPLE.name());
    }

    @Test
    void shouldReturnParsedQueryTypeForSearch() throws URISyntaxException, UnsupportedEncodingException {
      mockQueryResult(
        "hello AND bye",
        new QueryResult(0L, String.class, List.of(), QueryType.EXPERT)
      );

      JsonMockHttpResponse response = search("hello AND bye");

      JsonNode node = response.getContentAsJson();
      assertThat(node.get("queryType").asText()).isEqualTo(QueryType.EXPERT.name());
    }

    @Test
    void shouldReturnSimpleQueryTypeForCount() throws URISyntaxException, UnsupportedEncodingException {
      when(
        searchEngine.forType("string")
          .search()
          .count("hello")
      ).thenReturn(new QueryCountResult(String.class, 2L, QueryType.SIMPLE));

      JsonMockHttpResponse response = count("hello");

      JsonNode node = response.getContentAsJson();
      assertThat(node.get("queryType").asText()).isEqualTo(QueryType.SIMPLE.name());
    }

    @Test
    void shouldReturnParsedQueryTypeForCount() throws URISyntaxException, UnsupportedEncodingException {
      when(
        searchEngine.forType("string")
          .search()
          .count("hello AND bye")
      ).thenReturn(new QueryCountResult(String.class, 2L, QueryType.EXPERT));

      JsonMockHttpResponse response = count("hello AND bye");

      JsonNode node = response.getContentAsJson();
      assertThat(node.get("queryType").asText()).isEqualTo(QueryType.EXPERT.name());
    }

    @Test
    void shouldReturnEmbeddedRepository() throws UnsupportedEncodingException, URISyntaxException {
      Repository heartOfGold = RepositoryTestData.createHeartOfGold("git");
      heartOfGold.setId("42");
      when(repositoryManager.get("42")).thenReturn(heartOfGold);

      Hit hit = new Hit("21", "42", 21f, Collections.emptyMap());
      QueryResult result = new QueryResult(1L, String.class, Collections.singletonList(hit), null);

      mockQueryResult("hello", result);
      JsonMockHttpResponse response = search("hello");

      JsonNode hitNode = response.getContentAsJson().get("_embedded").get("hits").get(0);
      JsonNode repositoryNode = hitNode.get("_embedded").get("repository");

      assertThat(repositoryNode.get("namespace").asText()).isEqualTo(heartOfGold.getNamespace());
      assertThat(repositoryNode.get("name").asText()).isEqualTo(heartOfGold.getName());
      assertThat(repositoryNode.get("type").asText()).isEqualTo(heartOfGold.getType());
      assertThat(repositoryNode.get("_links").get("self").get("href").asText()).isEqualTo("/v2/repositories/hitchhiker/HeartOfGold");
    }
  }

  @Nested
  class WithScope {

    private final Repository repository1 = new Repository("1", "git", "space", "hog");
    private final Repository repository2 = new Repository("2", "git", "space", "hitchhiker");
    private final Repository repository3 = new Repository("3", "git", "earth", "42");
    @Mock
    private QueryBuilder<Object> internalQueryBuilder;

    @BeforeEach
    void mockRepositories() {
      lenient().when(repositoryManager.getAll())
        .thenReturn(
          List.of(
            repository1,
            repository2,
            repository3
          )
        );
      lenient().when(repositoryManager.get(new NamespaceAndName("space", "hog")))
        .thenReturn(repository1);
    }

    @BeforeEach
    void mockSearchResult() {
      when(
        searchEngine.forType("string")
          .search()
          .start(0)
          .limit(10)
      ).thenReturn(internalQueryBuilder);
      when(internalQueryBuilder.filter(any())).thenReturn(internalQueryBuilder);
      when(
        internalQueryBuilder.execute("Hello")
      ).thenReturn(result(2L, "Hello", "Hello Again"));
    }

    @Test
    void shouldReturnResultsScopedToNamespace() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/v2/search/query/space/string?q=Hello");
      JsonMockHttpResponse response = new JsonMockHttpResponse();
      dispatcher.invoke(request, response);

      JsonNode hits = response.getContentAsJson().get("_embedded").get("hits");
      assertThat(hits.size()).isEqualTo(2);

      verify(internalQueryBuilder).filter(repository1);
      verify(internalQueryBuilder).filter(repository2);
      verify(internalQueryBuilder, never()).filter(repository3);
    }

    @Test
    void shouldReturnResultsScopedToRepository() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/v2/search/query/space/hog/string?q=Hello");
      JsonMockHttpResponse response = new JsonMockHttpResponse();
      dispatcher.invoke(request, response);

      JsonNode hits = response.getContentAsJson().get("_embedded").get("hits");
      assertThat(hits.size()).isEqualTo(2);

      verify(internalQueryBuilder).filter(repository1);
      verify(internalQueryBuilder, never()).filter(repository2);
      verify(internalQueryBuilder, never()).filter(repository3);
    }
  }
}
