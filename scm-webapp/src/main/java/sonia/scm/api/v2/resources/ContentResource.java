package sonia.scm.api.v2.resources;

import com.github.sdorra.spotter.ContentType;
import com.github.sdorra.spotter.ContentTypes;
import com.github.sdorra.spotter.Language;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PathNotFoundException;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public class ContentResource {

  private final RepositoryServiceFactory servicefactory;

  @Inject
  public ContentResource(RepositoryServiceFactory servicefactory) {
    this.servicefactory = servicefactory;
  }

  @GET
  @Path("{revision}/{path: .*}")
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path) {
    try (RepositoryService repositoryService = servicefactory.create(new NamespaceAndName(namespace, name))) {
      try {
        byte[] content = getContent(revision, path, repositoryService);
        Response.ResponseBuilder responseBuilder = Response.ok(content);
        appendContentType(path, content, responseBuilder);
        return responseBuilder.build();
      } catch (PathNotFoundException e) {
        return Response.status(404).build();
      } catch (IOException e) {
        e.printStackTrace();
        return Response.status(500).entity(e.getMessage()).build();
      } catch (RepositoryException e) {
        e.printStackTrace();
        return Response.status(500).entity(e.getMessage()).build();
      }
    } catch (RepositoryNotFoundException e) {
      return Response.status(404).build();
    }
  }

  @HEAD
  @Path("{revision}/{path: .*}")
  public Response metadata(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path) {
    try (RepositoryService repositoryService = servicefactory.create(new NamespaceAndName(namespace, name))) {
      try {
        byte[] content = getContent(revision, path, repositoryService);

        Response.ResponseBuilder responseBuilder = Response.ok();

        appendContentType(path, content, responseBuilder);
        return responseBuilder.build();
      } catch (PathNotFoundException e) {
        return Response.status(404).build();
      } catch (IOException e) {
        e.printStackTrace();
        return Response.status(500).entity(e.getMessage()).build();
      } catch (RepositoryException e) {
        e.printStackTrace();
        return Response.status(500).entity(e.getMessage()).build();
      }
    } catch (RepositoryNotFoundException e) {
      return Response.status(404).build();
    }
  }

  private void appendContentType(String path, byte[] content, Response.ResponseBuilder responseBuilder) {
    ContentType contentType = ContentTypes.detect(path, content);
    System.out.println("Content-Type: " + contentType);

    Optional<Language> language = contentType.getLanguage();
    if (language.isPresent()) {
      responseBuilder.header("Content-Type", contentType);
    }
    responseBuilder.header("Content-Length", content.length);
  }

  private byte[] getContent(@PathParam("revision") String revision, @PathParam("path") String path, RepositoryService repositoryService) throws IOException, RepositoryException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    repositoryService.getCatCommand().setRevision(revision).retriveContent(outputStream, path);
    return outputStream.toByteArray();
  }
}
