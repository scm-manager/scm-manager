package sonia.scm.api.v2;


import javax.ws.rs.Priorities;

/**
 * A collection of filter priorities used by custom {@link javax.ws.rs.container.ContainerResponseFilter}s.
 */
final class FilterPriorities {

  /**
   * Other filters depend on already marshalled {@link com.fasterxml.jackson.databind.JsonNode} trees. Thus, JSON
   * marshalling has to happen before those filters
   */
  static final int JSON_MARSHALLING = Priorities.USER + 1000;
  static final int FIELD_FILTER = JSON_MARSHALLING - 1000;

  private FilterPriorities() {
  }
}
