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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.search.IndexNames;
import sonia.scm.search.QueryCountResult;
import sonia.scm.search.QueryResult;
import sonia.scm.search.SearchEngine;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collections;

@Path(SearchResource.PATH)
@OpenAPIDefinition(tags = {
  @Tag(name = "Search", description = "Search related endpoints")
})
public class SearchResource {

  static final String PATH = "v2/search";

  private final SearchEngine engine;
  private final QueryResultMapper mapper;

  @Inject
  public SearchResource(SearchEngine engine, QueryResultMapper mapper) {
    this.engine = engine;
    this.mapper = mapper;
  }

  @GET
  @Path("query/{type}")
  @Produces(VndMediaType.QUERY_RESULT)
  @Operation(
    summary = "Query result",
    description = "Returns a collection of matched hits.",
    tags = "Search",
    operationId = "search_query"
  )
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
  @Parameter(
    name = "query",
    description = "The search expression",
    required = true
  )
  @Parameter(
    name = "page",
    description = "The requested page number of the search results (zero based, defaults to 0)"
  )
  @Parameter(
    name = "pageSize",
    description = "The maximum number of results per page (defaults to 10)"
  )
  @Parameter(
    name = "countOnly",
    description = "Do not return hits, return only count of pages and hits"
  )
  public QueryResultDto query(@Valid @BeanParam SearchParameters params) {
    if (params.isCountOnly()) {
      return count(params);
    }
    return search(params);
  }

  private QueryResultDto search(SearchParameters params) {
    QueryResult result = engine.search(IndexNames.DEFAULT)
      .start(params.getPage() * params.getPageSize())
      .limit(params.getPageSize())
      .execute(params.getType(), params.getQuery());

    return mapper.map(params, result);
  }

  private QueryResultDto count(SearchParameters params) {
    QueryCountResult result = engine.search(IndexNames.DEFAULT)
      .count(params.getType(), params.getQuery());

    return mapper.map(params, new QueryResult(result.getTotalHits(), result.getType(), Collections.emptyList()));
  }

}
