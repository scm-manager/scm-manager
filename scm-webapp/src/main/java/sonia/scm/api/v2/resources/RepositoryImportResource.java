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
import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInputImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.importexport.FromBundleImporter;
import sonia.scm.importexport.FromUrlImporter;
import sonia.scm.importexport.FullScmRepositoryImporter;
import sonia.scm.importexport.RepositoryImportExportEncryption;
import sonia.scm.importexport.RepositoryImportLoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.web.VndMediaType;
import sonia.scm.web.api.DtoValidator;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RepositoryImportResource {

  private static final Logger logger = LoggerFactory.getLogger(RepositoryImportResource.class);

  private final RepositoryDtoToRepositoryMapper mapper;
  private final ResourceLinks resourceLinks;
  private final FullScmRepositoryImporter fullScmRepositoryImporter;
  private final RepositoryImportExportEncryption repositoryImportExportEncryption;
  private final RepositoryImportDtoToRepositoryImportParametersMapper importParametersMapper;
  private final FromUrlImporter fromUrlImporter;
  private final FromBundleImporter fromBundleImporter;
  private final RepositoryImportLoggerFactory importLoggerFactory;

  @Inject
  public RepositoryImportResource(RepositoryDtoToRepositoryMapper mapper,
                                  ResourceLinks resourceLinks,
                                  FullScmRepositoryImporter fullScmRepositoryImporter,
                                  RepositoryImportDtoToRepositoryImportParametersMapper importParametersMapper,
                                  RepositoryImportExportEncryption repositoryImportExportEncryption, FromUrlImporter fromUrlImporter,
                                  FromBundleImporter fromBundleImporter,
                                  RepositoryImportLoggerFactory importLoggerFactory) {
    this.mapper = mapper;
    this.resourceLinks = resourceLinks;
    this.fullScmRepositoryImporter = fullScmRepositoryImporter;
    this.repositoryImportExportEncryption = repositoryImportExportEncryption;
    this.importParametersMapper = importParametersMapper;
    this.fromUrlImporter = fromUrlImporter;
    this.fromBundleImporter = fromBundleImporter;
    this.importLoggerFactory = importLoggerFactory;
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
    headers = @Header(
      name = "Location",
      description = "uri to the created repository",
      schema = @Schema(type = "string")
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
                                @Valid RepositoryImportResource.RepositoryImportFromUrlDto request) {
    if (!type.equals(request.getType())) {
      throw new WebApplicationException("type of import url and repository does not match", Response.Status.BAD_REQUEST);
    }

    Repository repository = fromUrlImporter.importFromUrl(importParametersMapper.map(request), mapper.map(request));

    return Response.created(URI.create(resourceLinks.repository().self(repository.getNamespace(), repository.getName()))).build();
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
    description = "not authorized, the current user has no privileges to import repositories"
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
    Repository createdRepository = importFullRepositoryFromInput(input);
    return Response.created(URI.create(resourceLinks.repository().self(createdRepository.getNamespace(), createdRepository.getName()))).build();
  }

  @GET
  @Path("log/{logId}")
  @Produces(MediaType.TEXT_PLAIN)
  public StreamingOutput getImportLog(@PathParam("logId") String logId) throws IOException {
    importLoggerFactory.checkCanReadLog(logId);
    return out -> importLoggerFactory.getLog(logId, out);
  }

  private Repository importFullRepositoryFromInput(MultipartFormDataInput input) {
    Map<String, List<InputPart>> formParts = input.getFormDataMap();
    InputStream inputStream = extractInputStream(formParts);
    RepositoryImportFromFileDto repositoryDto = extractRepositoryDto(formParts);

    return fullScmRepositoryImporter.importFromStream(mapper.map(repositoryDto), inputStream, repositoryDto.getPassword());
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
    RepositoryImportFromFileDto repositoryDto = extractRepositoryDto(formParts);
    if (!Strings.isNullOrEmpty(repositoryDto.getPassword())) {
      inputStream = decryptInputStream(inputStream, repositoryDto.getPassword());
    }

    if (!type.equals(repositoryDto.getType())) {
      throw new WebApplicationException("type of import url and repository does not match", Response.Status.BAD_REQUEST);
    }

    Repository repository = mapper.map(repositoryDto);

    repository = fromBundleImporter.importFromBundle(compressed, inputStream, repository);

    return repository;
  }

  private InputStream decryptInputStream(InputStream inputStream, String password) {
    try {
      return repositoryImportExportEncryption.decrypt(inputStream, password);
    } catch (IOException e) {
      throw new ImportFailedException(ContextEntry.ContextBuilder.noContext(), "import failed", e);
    }
  }

  private RepositoryImportFromFileDto extractRepositoryDto(Map<String, List<InputPart>> formParts) {
    RepositoryImportFromFileDto repositoryDto = extractFromInputPart(formParts.get("repository"), RepositoryImportFromFileDto.class);
    checkNotNull(repositoryDto, "repository data is required");
    DtoValidator.validate(repositoryDto);
    return repositoryDto;
  }

  private void checkNotNull(Object object, String errorMessage) {
    if (object == null) {
      throw new WebApplicationException(errorMessage, 400);
    }
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
  public static class RepositoryImportFromUrlDto extends RepositoryDto implements ImportRepositoryFromUrlDto {
    @NotEmpty
    private String importUrl;
    private String username;
    private String password;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @SuppressWarnings("java:S2160")
  public static class RepositoryImportFromFileDto extends RepositoryDto implements ImportRepositoryFromFileDto {
    private String password;
  }

  interface ImportRepositoryFromUrlDto extends CreateRepositoryDto {
    @NotEmpty
    String getImportUrl();

    String getUsername();

    String getPassword();
  }

  interface ImportRepositoryFromFileDto extends CreateRepositoryDto {
    String getPassword();
  }
}
