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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.hibernate.validator.constraints.Length;
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.BranchDetailsCommandBuilder;
import sonia.scm.repository.api.BranchDetailsCommandResult;
import sonia.scm.repository.api.CommandNotSupportedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.constraints.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static sonia.scm.repository.Branch.VALID_BRANCH_NAMES;

public class BranchDetailsResource {

  private final RepositoryServiceFactory serviceFactory;
  private final BranchDetailsMapper mapper;
  private final ResourceLinks resourceLinks;

  @Inject
  public BranchDetailsResource(RepositoryServiceFactory serviceFactory, BranchDetailsMapper mapper, ResourceLinks resourceLinks) {
    this.serviceFactory = serviceFactory;
    this.mapper = mapper;
    this.resourceLinks = resourceLinks;
  }

  /**
   * Returns branch details for given branch.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace  the namespace of the repository
   * @param name       the name of the repository
   * @param branchName the name of the branch
   */
  @GET
  @Path("{branch}")
  @Produces(VndMediaType.BRANCH_DETAILS)
  @Operation(summary = "Get single branch details", description = "Returns details of a single branch.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.BRANCH_DETAILS,
      schema = @Schema(implementation = BranchDetailsDto.class)
    )
  )
  @ApiResponse(responseCode = "400", description = "branches not supported for given repository")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the branch")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no branch with the specified name for the repository available or repository found",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getBranchDetails(
    @PathParam("namespace") String namespace,
    @PathParam("name") String name,
    @Length(min = 1, max = 100) @Pattern(regexp = VALID_BRANCH_NAMES) @PathParam("branch") String branchName
  ) {
    try (RepositoryService service = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      BranchDetailsCommandResult result = service.getBranchDetailsCommand().execute(branchName);
      BranchDetailsDto dto = mapper.map(service.getRepository(), branchName, result.getDetails());
      return Response.ok(dto).build();
    } catch (CommandNotSupportedException ex) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  /**
   * Returns branch details for given branches.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace the namespace of the repository
   * @param name      the name of the repository
   * @param branches  a comma-seperated list of branches
   */
  @GET
  @Path("")
  @Produces(VndMediaType.BRANCH_DETAILS_COLLECTION)
  @Operation(summary = "Get multiple branch details", description = "Returns a collection of branch details.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.BRANCH_DETAILS_COLLECTION,
      schema = @Schema(implementation = HalRepresentation.class)
    )
  )
  @ApiResponse(responseCode = "400", description = "branches not supported for given repository")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the branch")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no branch with the specified name for the repository available or repository found",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getBranchDetailsCollection(
    @PathParam("namespace") String namespace,
    @PathParam("name") String name,
    @QueryParam("branches") List<@Length(min = 1, max = 100) @Pattern(regexp = VALID_BRANCH_NAMES) String> branches
  ) {
    try (RepositoryService service = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      List<BranchDetailsDto> dtos = getBranchDetailsDtos(service, branches);
      Links links = Links.linkingTo().self(resourceLinks.branchDetailsCollection().self(namespace, name)).build();
      Embedded embedded = Embedded.embeddedBuilder().with("branchDetails", dtos).build();

      return Response.ok(new HalRepresentation(links, embedded)).build();
    } catch (CommandNotSupportedException ex) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  private List<BranchDetailsDto> getBranchDetailsDtos(RepositoryService service, Collection<String> branches) {
    List<BranchDetailsDto> dtos = new ArrayList<>();
    if (!branches.isEmpty()) {
      BranchDetailsCommandBuilder branchDetailsCommand = service.getBranchDetailsCommand();
      for (String branch : branches) {
        try {
          BranchDetailsCommandResult result = branchDetailsCommand.execute(branch);
          dtos.add(mapper.map(service.getRepository(), branch, result.getDetails()));
        } catch (NotFoundException e) {
          // we simply omit details for branches that do not exist
        }
      }
    }
    return dtos;
  }
}
