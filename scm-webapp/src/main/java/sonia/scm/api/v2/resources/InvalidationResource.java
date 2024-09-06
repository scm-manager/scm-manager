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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.search.IndexRebuilder;
import sonia.scm.web.VndMediaType;

@OpenAPIDefinition(tags = {
  @Tag(name = "Invalidations", description = "Invalidations of different resources like caches and search index")
})
@Path("v2/invalidations")
public class InvalidationResource {

  private final CacheManager cacheManager;
  private final IndexRebuilder indexRebuilder;

  @Inject
  public InvalidationResource(CacheManager cacheManager, IndexRebuilder indexRebuilder) {
    this.cacheManager = cacheManager;
    this.indexRebuilder = indexRebuilder;
  }

  @POST
  @Path("/caches")
  @Operation(
    summary = "Invalidates the caches of every store",
    description = "Deletes every cached object of every store from the cache",
    tags = "Invalidations"
  )
  @ApiResponse(responseCode = "204", description = "Invalidated cache successfully")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write:global\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void invalidateCaches() {
    ConfigurationPermissions.write("global").check();
    cacheManager.clearAllCaches();
  }

  @POST
  @Path("/search-index")
  @Operation(
    summary = "Invalidates the search index",
    description = "Invalidates the search index, by completely recreating it",
    tags = "Invalidations"
  )
  @ApiResponse(responseCode = "204", description = "Invalidated search index successfully")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write:global\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void invalidateSearchIndex() {
    ConfigurationPermissions.write("global").check();
    invalidateCaches();
    indexRebuilder.rebuildAll();
  }
}
