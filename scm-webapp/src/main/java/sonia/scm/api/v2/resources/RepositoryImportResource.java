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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shiro.SecurityUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInputImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.HandlerEventType;
import sonia.scm.Type;
import sonia.scm.event.ScmEventBus;
import sonia.scm.importer.FullScmRepositoryImporter;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryImportEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.PullCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.IOUtil;
import sonia.scm.util.ValidationUtil;
import sonia.scm.web.VndMediaType;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static sonia.scm.api.v2.resources.RepositoryTypeSupportChecker.checkSupport;
import static sonia.scm.api.v2.resources.RepositoryTypeSupportChecker.type;

public class RepositoryImportResource {

  private static final Logger logger = LoggerFactory.getLogger(RepositoryImportResource.class);

  private final RepositoryManager manager;
  private final RepositoryDtoToRepositoryMapper mapper;
  private final RepositoryServiceFactory serviceFactory;
  private final ResourceLinks resourceLinks;
  private final ScmEventBus eventBus;
  private final FullScmRepositoryImporter fullScmRepositoryImporter;

  @Inject
  public RepositoryImportResource(RepositoryManager manager,
                                  RepositoryDtoToRepositoryMapper mapper,
                                  RepositoryServiceFactory serviceFactory,
                                  ResourceLinks resourceLinks,
                                  ScmEventBus eventBus,
                                  FullScmRepositoryImporter fullScmRepositoryImporter) {
    this.manager = manager;
    this.mapper = mapper;
    this.serviceFactory = serviceFactory;
    this.resourceLinks = resourceLinks;
    this.eventBus = eventBus;
    this.fullScmRepositoryImporter = fullScmRepositoryImporter;
  }

  /**
   * Imports a external repository which is accessible via url. The method can
   * only be used, if the repository type supports the {@link Command#PULL}. The
   * method will return a location header with the url to the imported
   * repository.
   *
   * @param uriInfo uri info
   * @param type    repository type
   * @param request request object
   * @return empty response with location header which points to the imported
   * repository
   * @since 2.11.0
   */
  @POST
  @Path("{type}/url")
  @Consumes(VndMediaType.REPOSITORY)
  @Operation(summary = "Import repository from url", description = "Imports the repository for the given url.", tags = "Repository")
  @ApiResponse(
    responseCode = "201",
    description = "Repository import was successful",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY,
      schema = @Schema(implementation = ImportRepositoryDto.class)
    )
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
  public Response importFromUrl(@Context UriInfo uriInfo,
                                @Pattern(regexp = "\\w{1,10}") @PathParam("type") String type,
                                @Valid RepositoryImportDto request) {
    RepositoryPermissions.create().check();

    Type t = type(manager, type);
    if (!t.getName().equals(request.getType())) {
      throw new WebApplicationException("type of import url and repository does not match", Response.Status.BAD_REQUEST);
    }
    checkSupport(t, Command.PULL);

    logger.info("start {} import for external url {}", type, request.getImportUrl());

    Repository repository = mapper.map(request);
    repository.setPermissions(singletonList(new RepositoryPermission(SecurityUtils.getSubject().getPrincipal().toString(), "OWNER", false)));

    try {
      repository = manager.create(
        repository,
        pullChangesFromRemoteUrl(request)
      );
      eventBus.post(new RepositoryImportEvent(HandlerEventType.MODIFY, repository, false));

      return Response.created(URI.create(resourceLinks.repository().self(repository.getNamespace(), repository.getName()))).build();
    } catch (Exception e) {
      eventBus.post(new RepositoryImportEvent(HandlerEventType.MODIFY, repository, true));
      throw e;
    }
  }

  @VisibleForTesting
  Consumer<Repository> pullChangesFromRemoteUrl(RepositoryImportDto request) {
    return repository -> {
      try (RepositoryService service = serviceFactory.create(repository)) {
        PullCommandBuilder pullCommand = service.getPullCommand();
        if (!Strings.isNullOrEmpty(request.getUsername()) && !Strings.isNullOrEmpty(request.getPassword())) {
          pullCommand
            .withUsername(request.getUsername())
            .withPassword(request.getPassword());
        }

        pullCommand.pull(request.getImportUrl());
      } catch (IOException e) {
        throw new InternalRepositoryException(repository, "Failed to import from remote url", e);
      }
    };
  }

  /**
   * Imports a external repository via dump. The method can
   * only be used, if the repository type supports the {@link Command#UNBUNDLE}. The
   * method will return a location header with the url to the imported
   * repository.
   *
   * @param uriInfo    uri info
   * @param type       repository type
   * @param input      multi part form data which should contain a valid repository dto and the input stream of the bundle
   * @param compressed true if the bundle is gzip compressed
   * @return empty response with location header which points to the imported
   * repository
   * @since 2.12.0
   */
  @POST
  @Path("{type}/bundle")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Operation(summary = "Import repository from bundle", description = "Imports the repository from the provided bundle.", tags = "Repository")
  @ApiResponse(
    responseCode = "201",
    description = "Repository import was successful"
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
  public Response importFromBundle(@Context UriInfo uriInfo,
                                   @Pattern(regexp = "\\w{1,10}") @PathParam("type") String type,
                                   MultipartFormDataInput input,
                                   @QueryParam("compressed") @DefaultValue("false") boolean compressed) {
    RepositoryPermissions.create().check();
    Repository repository = doImportFromBundle(type, input, compressed);

    return Response.created(URI.create(resourceLinks.repository().self(repository.getNamespace(), repository.getName()))).build();
  }

  /**
   * Imports a repository as SCM-Manager provided import archive. The method can
   * only be used, if the repository type supports the {@link Command#UNBUNDLE}. The
   * method will return a location header with the url to the imported
   * repository.
   *
   * @param uriInfo uri info
   * @param type    repository type
   * @param input   multi part form data which should contain a valid repository dto and the input stream of the bundle
   * @return empty response with location header which points to the imported
   * repository
   * @since 2.13.0
   */
  @POST
  @Path("{type}/full")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Operation(
    summary = "Import repository from SCM-Manager repository archive",
    description = "Imports the repository with metadata from the provided bundle.",
    tags = "Repository"
  )
  @ApiResponse(
    responseCode = "201",
    description = "Repository import was successful"
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
  public Response importFullRepository(@Context UriInfo uriInfo,
                                       @Pattern(regexp = "\\w{1,10}") @PathParam("type") String type,
                                       MultipartFormDataInput input) {
    RepositoryPermissions.create().check();
    RepositoryDto repositoryDto = importFullRepositoryFromInput(input);
    return Response.created(URI.create(resourceLinks.repository().self(repositoryDto.getNamespace(), repositoryDto.getName()))).build();
  }

  private RepositoryDto importFullRepositoryFromInput(MultipartFormDataInput input) {
    Map<String, List<InputPart>> formParts = input.getFormDataMap();
    InputStream inputStream = extractInputStream(formParts);
    RepositoryDto repositoryDto = extractRepositoryDto(formParts);

    fullScmRepositoryImporter.importFromFile(mapper.map(repositoryDto), inputStream);
    return repositoryDto;
  }

  /**
   * Start bundle import.
   *
   * @param type       repository type
   * @param input      multi part form data
   * @param compressed true if the bundle is gzip compressed
   * @return imported repository
   */
  private Repository doImportFromBundle(String type, MultipartFormDataInput input, boolean compressed) {
    Map<String, List<InputPart>> formParts = input.getFormDataMap();
    InputStream inputStream = extractInputStream(formParts);
    RepositoryDto repositoryDto = extractRepositoryDto(formParts);

    Type t = type(manager, type);
    checkSupport(t, Command.UNBUNDLE);

    Repository repository = mapper.map(repositoryDto);
    repository.setPermissions(singletonList(
      new RepositoryPermission(SecurityUtils.getSubject().getPrincipal().toString(), "OWNER", false)
    ));

    try {
      repository = manager.create(
        repository,
        unbundleImport(inputStream, compressed)
      );
      eventBus.post(new RepositoryImportEvent(HandlerEventType.MODIFY, repository, false));

    } catch (Exception e) {
      eventBus.post(new RepositoryImportEvent(HandlerEventType.MODIFY, repository, true));
      throw e;
    }

    return repository;
  }

  @VisibleForTesting
  Consumer<Repository> unbundleImport(InputStream inputStream, boolean compressed) {
    return repository -> {
      File file = null;
      try (RepositoryService service = serviceFactory.create(repository)) {
        file = File.createTempFile("scm-import-", ".bundle");
        long length = Files.asByteSink(file).writeFrom(inputStream);
        logger.info("copied {} bytes to temp, start bundle import", length);
        service.getUnbundleCommand().setCompressed(compressed).unbundle(file);
      } catch (IOException e) {
        throw new InternalRepositoryException(repository, "Failed to import from bundle", e);
      } finally {
        try {
          IOUtil.delete(file);
        } catch (IOException ex) {
          logger.warn("could not delete temporary file", ex);
        }
      }
    };
  }

  private RepositoryDto extractRepositoryDto(Map<String, List<InputPart>> formParts) {
    RepositoryDto repositoryDto = extractFromInputPart(formParts.get("repository"), RepositoryDto.class);
    checkNotNull(repositoryDto, "repository data is required");
    checkArgument(!Strings.isNullOrEmpty(repositoryDto.getName()), "request does not contain name of the repository");
    return repositoryDto;
  }

  private InputStream extractInputStream(Map<String, List<InputPart>> formParts) {
    InputStream inputStream = extractFromInputPart(formParts.get("bundle"), InputStream.class);
    checkNotNull(inputStream, "bundle inputStream is required");
    return inputStream;
  }

  private <T> T extractFromInputPart(List<InputPart> input, Class<T> type) {
    try {
      if (input != null && !input.isEmpty()) {
        if (type == InputStream.class) {
          return (T) ((MultipartInputImpl.PartImpl) input.get(0)).getBody();
        }
        String content = new ByteSource() {
          @Override
          public InputStream openStream() throws IOException {
            return ((MultipartInputImpl.PartImpl) input.get(0)).getBody();
          }
        }.asCharSource(UTF_8).read();
        try (JsonParser parser = new JsonFactory().createParser(content)) {
          parser.setCodec(new ObjectMapper());
          return parser.readValueAs(type);
        }
      }
    } catch (IOException ex) {
      logger.debug("Could not extract repository from input");
    }
    return null;
  }
  @Getter
  @Setter
  @NoArgsConstructor
  @SuppressWarnings("java:S2160")
  public static class RepositoryImportDto extends RepositoryDto implements ImportRepositoryDto {

    @NotEmpty
    private String importUrl;
    private String username;
    private String password;

    RepositoryImportDto(Links links, Embedded embedded) {
      super(links, embedded);
    }
  }

  interface ImportRepositoryDto {
    String getNamespace();

    @Pattern(regexp = ValidationUtil.REGEX_REPOSITORYNAME)
    String getName();

    @NotEmpty
    String getType();

    @Email
    String getContact();

    String getDescription();

    @NotEmpty
    String getImportUrl();

    String getUsername();

    String getPassword();
  }
}
