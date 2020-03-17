/**
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public abstract class JsonEnricherBase implements JsonEnricher {

  private final ObjectMapper objectMapper;

  protected JsonEnricherBase(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  protected boolean resultHasMediaType(String mediaType, JsonEnricherContext context) {
    return mediaType.equalsIgnoreCase(context.getResponseMediaType().toString());
  }

  protected JsonNode value(Object object) {
    return objectMapper.convertValue(object, JsonNode.class);
  }

  protected ObjectNode createObject() {
    return objectMapper.createObjectNode();
  }

  protected ObjectNode createObject(Map<String, Object> values) {
    ObjectNode object = createObject();

    values.forEach((key, value) -> object.set(key, value(value)));

    return object;
  }

  protected void addPropertyNode(JsonNode parent, String newKey, JsonNode child) {
    ((ObjectNode) parent).set(newKey, child);
  }
}
