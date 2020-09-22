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
import sonia.scm.admin.ReleaseInfo;
import sonia.scm.admin.ReleaseInfoMapper;
import sonia.scm.admin.ReleaseVersionChecker;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Optional;

@OpenAPIDefinition(tags = {
  @Tag(name = "AdminInfo", description = "Admin information endpoints")
})
@Path("")
public class AdminInfoResource {

  private final ReleaseVersionChecker checker;
  private final ReleaseInfoMapper mapper;

  @Inject
  public AdminInfoResource(ReleaseVersionChecker checker, ReleaseInfoMapper mapper) {
    this.checker = checker;
    this.mapper = mapper;
  }

  /**
   * Checks for a newer core version of SCM-Manager.
   */
  @GET
  @Path("releaseInfo")
  @Produces(VndMediaType.ADMIN_INFO)
  @Operation(summary = "Returns release info.", description = "Returns information about the latest release if a newer version of SCM-Manager is available.", tags = "AdminInfo")
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the information")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public ReleaseInfoDto getReleaseInfo() {
    Optional<ReleaseInfo> releaseInfo = checker.checkForNewerVersion();
    return releaseInfo.map(mapper::map).orElse(null);
  }
}
