package sonia.scm.repository.spi;

import sonia.scm.repository.Repository;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

public abstract class DefaultHttpScmProtocol implements HttpScmProtocol {

  private final Repository repository;

  protected DefaultHttpScmProtocol(Repository repository) {
    this.repository = repository;
  }

  @Override
  public String getUrl(Repository repository, UriInfo uriInfo) {
    return uriInfo.getBaseUri().resolve(URI.create("../../" + this.repository.getType() + "/" + this.repository.getNamespace() + "/" + this.repository.getName())).toASCIIString();
  }
}
