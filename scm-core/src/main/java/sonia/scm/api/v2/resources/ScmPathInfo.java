package sonia.scm.api.v2.resources;

import java.net.URI;

public interface ScmPathInfo {

  String REST_API_PATH = "/api";

  URI getApiRestUri();

  default URI getRootUri() {
    return getApiRestUri().resolve("..");
  }
}
