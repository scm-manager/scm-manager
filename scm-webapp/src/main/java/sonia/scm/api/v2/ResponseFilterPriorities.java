/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.v2;


import jakarta.ws.rs.Priorities;

/**
 * A collection of filter priorities used by custom {@link jakarta.ws.rs.container.ContainerResponseFilter}s.
 * Higher number means earlier execution in the response filter chain.
 */
final class ResponseFilterPriorities {

  /**
   * Other filters depend on already marshalled {@link com.fasterxml.jackson.databind.JsonNode} trees. Thus, JSON
   * marshalling has to happen before those filters
   */
  static final int JSON_MARSHALLING = Priorities.USER + 1000;
  static final int FIELD_FILTER = Priorities.USER;

  static final int INVALID_ACCEPT_HEADER = JSON_MARSHALLING + 1000;

  private ResponseFilterPriorities() {
  }
}
