package sonia.scm.api.rest.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.util.IOUtil;

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
    } finally {
      IOUtil.close(repositoryService);
    }
  }
}
