package sonia.scm.web;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.core.MediaType;
import java.net.URI;

/**
 * Process data for the {@link JsonEnricher} extension point giving context for
 * post processing json results.
 */
public class JsonEnricherContext {

  private URI requestUri;
  private MediaType responseMediaType;
  private JsonNode responseEntity;

  public JsonEnricherContext(URI requestUri, MediaType responseMediaType, JsonNode responseEntity) {
    this.requestUri = requestUri;
    this.responseMediaType = responseMediaType;
    this.responseEntity = responseEntity;
  }

  /**
   * The URI of the originating request.
   */
  public URI getRequestUri() {
    return requestUri;
  }

  /**
   * The media type of the response. Using this you can determine the content of the result.
   * @see VndMediaType
   */
  public MediaType getResponseMediaType() {
    return responseMediaType;
  }

  /**
   * The json result represented by nodes, that can be modified.
   */
  public JsonNode getResponseEntity() {
    return responseEntity;
  }
}
