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
    return mediaType.equals(context.getResponseMediaType().toString());
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
    ((ObjectNode) parent).put(newKey, child);
  }
}
