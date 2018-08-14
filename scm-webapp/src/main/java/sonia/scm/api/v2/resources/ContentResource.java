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
import sonia.scm.util.IOUtil;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ContentResource {

  private static final Logger LOG = LoggerFactory.getLogger(ContentResource.class);
  private static final int HEAD_BUFFER_SIZE = 1024;

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public ContentResource(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  @GET
  @Path("{revision}/{path: .*}")
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path) {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      StreamingOutput stream = createStreamingOutput(namespace, name, revision, path, repositoryService);
      Response.ResponseBuilder responseBuilder = Response.ok(stream);
      return createContentHeader(namespace, name, revision, path, repositoryService, responseBuilder);
    } catch (RepositoryNotFoundException e) {
      LOG.debug("path '{}' not found in repository {}/{}", path, namespace, name, e);
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  private StreamingOutput createStreamingOutput(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path, RepositoryService repositoryService) {
    return os -> {
            try {
              repositoryService.getCatCommand().setRevision(revision).retriveContent(os, path);
              os.close();
            } catch (PathNotFoundException e) {
              LOG.debug("path '{}' not found in repository {}/{}", path, namespace, name, e);
              throw new WebApplicationException(Status.NOT_FOUND);
            } catch (RepositoryException e) {
              LOG.info("error reading repository resource {} from {}/{}", path, namespace, name, e);
              throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
            }
          };
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
    InputStream stream = repositoryService.getCatCommand().setRevision(revision).getStream(path);
    try {
      byte[] buffer = new byte[HEAD_BUFFER_SIZE];
      int length = stream.read(buffer);
      if (length < buffer.length) {
        return Arrays.copyOf(buffer, length);
      } else {
        return buffer;
      }
    } finally {
      IOUtil.close(stream);
    }
  }
}
