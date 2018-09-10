package sonia.scm.repository.api;

import java.net.URI;

/**
 * An ScmProtocol represents a concrete protocol provided by the SCM-Manager instance
 * to interact with a repository depending on its type. There may be multiple protocols
 * available for a repository type (eg. http and ssh).
 */
public interface ScmProtocol {

  /**
   * The type of the concrete protocol, eg. "http" or "ssh".
   */
  String getType();

  /**
   * The URL to access the repository providing this protocol.
   */
  String getUrl(URI baseUri);
}
