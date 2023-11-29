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
