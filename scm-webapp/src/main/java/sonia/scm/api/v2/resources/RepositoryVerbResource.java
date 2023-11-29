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

import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import sonia.scm.security.RepositoryPermissionProvider;
import sonia.scm.web.VndMediaType;

/**
 * RESTful Web Service Resource to get available repository verbs.
 */
@Path(RepositoryVerbResource.PATH)
public class RepositoryVerbResource {

  static final String PATH = "v2/repositoryVerbs/";

  private final RepositoryPermissionProvider repositoryPermissionProvider;
  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryVerbResource(RepositoryPermissionProvider repositoryPermissionProvider, ResourceLinks resourceLinks) {
    this.repositoryPermissionProvider = repositoryPermissionProvider;
    this.resourceLinks = resourceLinks;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_VERB_COLLECTION)
  @Operation(summary = "List of repository verbs", description = "Returns all repository-specific permissions.", hidden = true)
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY_VERB_COLLECTION,
      schema = @Schema(implementation = RepositoryVerbsDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public RepositoryVerbsDto getAll() {
    return new RepositoryVerbsDto(
      Links.linkingTo().self(resourceLinks.repositoryVerbs().self()).build(),
      repositoryPermissionProvider.availableVerbs()
    );
  }
}
