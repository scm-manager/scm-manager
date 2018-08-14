package sonia.scm.api.v2.resources;

import com.github.sdorra.spotter.ContentType;
import com.github.sdorra.spotter.ContentTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ContentResource {

  private static final Logger LOG = LoggerFactory.getLogger(ContentResource.class);

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public ContentResource(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  @GET
  @Path("{revision}/{path: .*}")
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path) {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
        StreamingOutput stream = os -> {
          try {
            repositoryService.getCatCommand().setRevision(revision).retriveContent(os, path);
          } catch (PathNotFoundException e) {
            LOG.debug("path '{}' not found in repository {}/{}", path, namespace, name, e);
            throw new WebApplicationException(Status.NOT_FOUND);
          } catch (RepositoryException e) {
            LOG.info("error reading repository resource {} from {}/{}", path, namespace, name, e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
          }
          os.close();
        };

      Response.ResponseBuilder responseBuilder = Response.ok(stream);
      return createContentHeader(namespace, name, revision, path, repositoryService, responseBuilder);
    } catch (RepositoryNotFoundException e) {
      LOG.debug("path '{}' not found in repository {}/{}", path, namespace, name, e);
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @HEAD
  @Path("{revision}/{path: .*}")
  public Response metadata(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path) {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
        Response.ResponseBuilder responseBuilder = Response.ok();
      return createContentHeader(namespace, name, revision, path, repositoryService, responseBuilder);
    } catch (RepositoryNotFoundException e) {
      LOG.debug("path '{}' not found in repository {}/{}", path, namespace, name, e);
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  private Response createContentHeader(String namespace, String name, String revision, String path, RepositoryService repositoryService, Response.ResponseBuilder responseBuilder) {
    try {
      appendContentHeader(path, getHead(revision, path, repositoryService), responseBuilder);
    } catch (PathNotFoundException e) {
      LOG.debug("path '{}' not found in repository {}/{}", path, namespace, name, e);
      return Response.status(Status.NOT_FOUND).build();
    } catch (IOException e) {
      LOG.info("error reading repository resource {} from {}/{}", path, namespace, name, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (RepositoryException e) {
      LOG.info("error reading repository resource {} from {}/{}", path, namespace, name, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
    return responseBuilder.build();
  }

  private void appendContentHeader(String path, byte[] head, Response.ResponseBuilder responseBuilder) {
    ContentType contentType = ContentTypes.detect(path, head);
    responseBuilder.header("Content-Type", contentType.getRaw());
    contentType.getLanguage().ifPresent(language -> responseBuilder.header("Language", language));
    responseBuilder.header("Content-Length", head.length);
  }

  private byte[] getHead(String revision, String path, RepositoryService repositoryService) throws IOException, RepositoryException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    repositoryService.getCatCommand().setRevision(revision).retriveContent(outputStream, path);
    return outputStream.toByteArray();
  }
}
