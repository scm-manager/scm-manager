package sonia.scm.api.v2.resources;

import java.net.URI;

public interface ScmPathInfo {
  URI getApiRestUri();

  default URI getRootUri() {
    return getApiRestUri().resolve("../..");
  }
}
