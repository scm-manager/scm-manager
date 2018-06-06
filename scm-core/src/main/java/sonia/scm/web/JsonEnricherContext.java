package sonia.scm.web;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.core.MediaType;
import java.net.URI;

public class JsonEnricherContext {

  private URI requestUri;
  private MediaType responseMediaType;
  private JsonNode responseEntity;

  public JsonEnricherContext(URI requestUri, MediaType responseMediaType, JsonNode responseEntity) {
    this.requestUri = requestUri;
    this.responseMediaType = responseMediaType;
    this.responseEntity = responseEntity;
  }

  public URI getRequestUri() {
    return requestUri;
  }

  public MediaType getResponseMediaType() {
    return responseMediaType;
  }

  public JsonNode getResponseEntity() {
    return responseEntity;
  }
}
