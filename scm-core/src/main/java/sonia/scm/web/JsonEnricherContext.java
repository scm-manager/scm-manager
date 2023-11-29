/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
