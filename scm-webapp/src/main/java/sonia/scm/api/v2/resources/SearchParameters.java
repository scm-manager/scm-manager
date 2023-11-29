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

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import lombok.Getter;

@Getter
class SearchParameters {

  @Context
  private UriInfo uriInfo;

  @NotNull
  @Size(min = 2)
  @QueryParam("q")
  @Parameter(
    name = "q",
    description = "The search expression",
    required = true,
    example = "query"
  )
  private String query;

  @Min(0)
  @QueryParam("page")
  @DefaultValue("0")
  @Parameter(
    name = "page",
    description = "The requested page number of the search results (zero based, defaults to 0)"
  )
  private int page = 0;

  @Min(1)
  @Max(100)
  @QueryParam("pageSize")
  @DefaultValue("10")
  @Parameter(
    name = "pageSize",
    description = "The maximum number of results per page (defaults to 10)"
  )
  private int pageSize = 10;

  @PathParam("type")
  @Parameter(
    name = "type",
    description = "The type to search for",
    example = "repository"
  )
  private String type;

  @QueryParam("countOnly")
  @Parameter(
    name = "countOnly",
    description = "If set to 'true', no results will be returned, only the count of hits and the page count"
  )
  private boolean countOnly = false;

  String getSelfLink() {
    return uriInfo.getAbsolutePath().toASCIIString();
  }

  String getNamespace() {
    return null;
  }

  String getRepositoryName() {
    return null;
  }
}
