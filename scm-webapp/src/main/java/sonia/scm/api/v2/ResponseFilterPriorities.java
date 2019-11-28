package sonia.scm.api.v2;


import javax.ws.rs.Priorities;

/**
 * A collection of filter priorities used by custom {@link javax.ws.rs.container.ContainerResponseFilter}s.
 * Higher number means earlier execution in the response filter chain.
 */
final class ResponseFilterPriorities {

  /**
   * Other filters depend on already marshalled {@link com.fasterxml.jackson.databind.JsonNode} trees. Thus, JSON
   * marshalling has to happen before those filters
   */
  static final int JSON_MARSHALLING = Priorities.USER + 1000;
  static final int FIELD_FILTER = Priorities.USER;

  private ResponseFilterPriorities() {
  }
}
