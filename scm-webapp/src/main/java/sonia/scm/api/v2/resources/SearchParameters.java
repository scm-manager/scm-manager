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
