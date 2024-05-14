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

import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.search.QueryBuilder;
import sonia.scm.search.QueryCountResult;
import sonia.scm.search.QueryResult;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SearchableType;
import sonia.scm.web.VndMediaType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Path(SearchResource.PATH)
@OpenAPIDefinition(tags = {
  @Tag(name = "Search", description = "Search related endpoints")
})
public class SearchResource {

  static final String PATH = "v2/search";

  private final SearchEngine engine;
  private final QueryResultMapper queryResultMapper;
  private final SearchableTypeMapper searchableTypeMapper;
  private final RepositoryManager repositoryManager;

  @Inject
  public SearchResource(SearchEngine engine, QueryResultMapper mapper, SearchableTypeMapper searchableTypeMapper, RepositoryManager repositoryManager) {
    this.engine = engine;
    this.queryResultMapper = mapper;
    this.searchableTypeMapper = searchableTypeMapper;
    this.repositoryManager = repositoryManager;
  }

  @Path("query")
  public SearchEndpoints query() {
    return new SearchEndpoints();
  }

  @Produces(VndMediaType.QUERY_RESULT)
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.QUERY_RESULT,
      schema = @Schema(implementation = QueryResultDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public class SearchEndpoints {

    @GET
    @Path("{type}")
    @Operation(
      summary = "Global query result",
      description = "Returns a collection of matched hits.",
      tags = "Search",
      operationId = "search_query"
    )
    public QueryResultDto globally(@Valid @BeanParam SearchParameters params) {
      if (params.isCountOnly()) {
        return count(params);
      }
      return search(params);
    }

    @GET
    @Path("{namespace}/{type}")
    @Operation(
      summary = "Query result for a namespace",
      description = "Returns a collection of matched hits limited to the namespace.",
      tags = "Search",
      operationId = "search_query_for_namespace"
    )
    public QueryResultDto forNamespace(@Valid @BeanParam SearchParametersLimitedToNamespace params) {
      if (params.isCountOnly()) {
        return count(params);
      }
      return search(params);
    }

    @GET
    @Path("{namespace}/{name}/{type}")
    @Operation(
      summary = "Query result for a repository",
      description = "Returns a collection of matched hits limited to the repository specified by namespace and name.",
      tags = "Search",
      operationId = "search_query_for_repository"
    )
    public QueryResultDto forRepository(@Valid @BeanParam SearchParametersLimitedToRepository params) {
      if (params.isCountOnly()) {
        return count(params);
      }
      return search(params);
    }

    private QueryResultDto search(SearchParameters params) {
      QueryBuilder<Object> queryBuilder = engine.forType(params.getType())
        .search()
        .start(params.getPage() * params.getPageSize())
        .limit(params.getPageSize());

      filterByContext(params, queryBuilder);

      return queryResultMapper.map(params, queryBuilder.execute(params.getQuery()));
    }

    private QueryResultDto count(SearchParameters params) {
      QueryBuilder<Object> queryBuilder = engine.forType(params.getType())
        .search();

      filterByContext(params, queryBuilder);

      QueryCountResult result = queryBuilder.count(params.getQuery());

      return queryResultMapper.map(
        params,
        new QueryResult(result.getTotalHits(), result.getType(), Collections.emptyList(), result.getQueryType())
      );
    }

    private void filterByContext(SearchParameters params, QueryBuilder<Object> queryBuilder) {
      if (!Strings.isNullOrEmpty(params.getNamespace())) {
        if (!Strings.isNullOrEmpty(params.getRepositoryName())) {
          Repository repository = repositoryManager.get(new NamespaceAndName(params.getNamespace(), params.getRepositoryName()));
          queryBuilder.filter(repository);
        } else {
          repositoryManager.getAll().stream()
            .filter(r -> r.getNamespace().equals(params.getNamespace()))
            .forEach(queryBuilder::filter);
        }
      }
    }
  }

  @Path("searchableTypes")
  public SearchableTypesEndpoints searchableTypes() {
    return new SearchableTypesEndpoints();
  }

  @Produces(VndMediaType.SEARCHABLE_TYPE_COLLECTION)
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.SEARCHABLE_TYPE_COLLECTION
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public class SearchableTypesEndpoints {

    @GET
    @Path("")
    @Operation(
      summary = "Globally searchable types",
      description = "Returns a collection of all searchable types.",
      tags = "Search",
      operationId = "searchable_types"
    )
    public Collection<SearchableTypeDto> globally() {
      return getTypes(t -> true);
    }

    @GET
    @Path("{namespace}")
    @Operation(
      summary = "Searchable types in a namespace",
      description = "Returns a collection of all searchable types when scoped to a namespace.",
      tags = "Search",
      operationId = "searchable_types_for_namespace"
    )
    public Collection<SearchableTypeDto> forNamespace(
      @Parameter(
        name = "namespace",
        description = "The namespace to get the types for"
      )
      @PathParam("namespace") String namespace) {
      return getTypes(SearchableType::limitableToNamespace);
    }

    @GET
    @Path("{namespace}/{name}")
    @Operation(
      summary = "Searchable types in a repository",
      description = "Returns a collection of all searchable types when scoped to a repository.",
      tags = "Search",
      operationId = "searchable_types_for_repository"
    )
    public Collection<SearchableTypeDto> forRepository(
      @Parameter(
        name = "namespace",
        description = "The namespace of the repository to get the types for"
      )
      @PathParam("namespace")
      String namespace,
      @Parameter(
        name = "name",
        description = "The name of the repository to get the types for"
      )
      @PathParam("name")
      String name
    ) {
      return getTypes(SearchableType::limitableToRepository);
    }

    private List<SearchableTypeDto> getTypes(Predicate<SearchableType> predicate) {
      return engine.getSearchableTypes().stream()
        .filter(predicate)
        .map(searchableTypeMapper::map)
        .collect(Collectors.toList());
    }
  }
}
