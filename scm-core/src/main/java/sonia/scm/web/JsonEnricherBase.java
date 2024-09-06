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
