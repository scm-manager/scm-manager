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

import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Type;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class RepositoryExportResource {

  private static final Logger logger = LoggerFactory.getLogger(RepositoryExportResource.class);

  private final RepositoryManager manager;
  private final RepositoryServiceFactory serviceFactory;
  private final ResourceLinks resourceLinks;
  private final ScmEventBus eventBus;

  @Inject
  public RepositoryExportResource(RepositoryManager manager,
                                  RepositoryServiceFactory serviceFactory,
                                  ResourceLinks resourceLinks,
                                  ScmEventBus eventBus) {
    this.manager = manager;
    this.serviceFactory = serviceFactory;
    this.resourceLinks = resourceLinks;
    this.eventBus = eventBus;
  }

  /**
   * Exports an existing repository without additional metadata. The method can
   * only be used, if the repository type supports the {@link Command#BUNDLE}.
   *
   * @param uriInfo uri info
   * @return empty response with location header which points to the imported
   * repository
   * @since 2.13.0
   */
  @GET
  @Path("{type}")
  @Consumes(VndMediaType.REPOSITORY)
  @Produces("application/octet-stream")
  @Operation(summary = "Exports the repository", description = "Exports the repository.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "Repository export was successful"
  )
  @ApiResponse(
    responseCode = "401",
    description = "not authenticated / invalid credentials"
  )
  @ApiResponse(
    responseCode = "403",
    description = "not authorized, the current user has no privileges to read the repository"
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response export(@Context UriInfo uriInfo,
                         @PathParam("namespace") String namespace,
                         @PathParam("name") String name,
                         @PathParam("type") String type,
                         @DefaultValue("false") @QueryParam("compressed") boolean compressed
  ) {
    Repository repository = manager.get(new NamespaceAndName(namespace, name));
    RepositoryPermissions.read().check(repository);

    Type repositoryType = type(type);
    checkSupport(repositoryType, Command.BUNDLE);

    logger.info("start {} export for repository {}/{}", repositoryType, namespace, name);
    StreamingOutput output = exportRepositoryAsOutputStream(repository);

    return Response
      .ok(output)
      .header("content-disposition", String.format("attachment; filename = %s-%s.dump", repository.getId(), repository.getName()))
      .build();
  }

  private StreamingOutput exportRepositoryAsOutputStream(Repository repository) {
    return os -> {
      try (RepositoryService service = serviceFactory.create(repository)) {
        service.getBundleCommand().bundle(os);
      } catch (IOException e) {
        throw new InternalRepositoryException(repository, "repository export failed", e);
      }
    };
  }

  private StreamingOutput exportRepositoryAsZippedOutputStream(Repository repository) {
    return os -> {
      try (RepositoryService service = serviceFactory.create(repository)) {
        service.getBundleCommand().bundle(os);


      } catch (IOException e) {
        throw new InternalRepositoryException(repository, "repository export failed", e);
      }
    };
  }

  /**
   * Check repository type for support for the given command.
   *
   * @param type repository type
   * @param cmd  command
   */
  private void checkSupport(Type type, Command cmd) {
    if (!(type instanceof RepositoryType)) {
      logger.warn("type {} is not a repository type", type.getName());

      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    Set<Command> cmds = ((RepositoryType) type).getSupportedCommands();

    if (!cmds.contains(cmd)) {
      logger.warn("type {} does not support this type of export",
        type.getName());

      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  private Type type(String type) {
    RepositoryHandler handler = manager.getHandler(type);

    if (handler == null) {
      logger.warn("no handler for type {} found", type);

      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    return handler.getType();
  }
}
