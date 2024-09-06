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

package sonia.scm.web;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.MediaType;

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
