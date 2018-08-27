package sonia.scm.api.rest.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.PathNotFoundException;
import sonia.scm.repository.RevisionNotFoundException;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.util.IOUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

public class BrowserStreamingOutput implements StreamingOutput {

  private static final Logger logger =
    LoggerFactory.getLogger(BrowserStreamingOutput.class);

  private final CatCommandBuilder builder;
  private final String path;
  private final RepositoryService repositoryService;

  public BrowserStreamingOutput(RepositoryService repositoryService,
                                CatCommandBuilder builder, String path) {
    this.repositoryService = repositoryService;
    this.builder = builder;
    this.path = path;
  }

  @Override
  public void write(OutputStream output) throws IOException {
    try {
      builder.retriveContent(output, path);
    } catch (PathNotFoundException ex) {
      if (logger.isWarnEnabled()) {
        logger.warn("could not find path {}", ex.getPath());
      }

      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } catch (RevisionNotFoundException ex) {
      if (logger.isWarnEnabled()) {
        logger.warn("could not find revision {}", ex.getRevision());
      }

      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } finally {
      IOUtil.close(repositoryService);
    }
  }
}
