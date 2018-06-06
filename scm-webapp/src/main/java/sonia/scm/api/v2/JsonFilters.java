package sonia.scm.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.Map;

public final class JsonFilters {

  private JsonFilters() {
  }

  public static void filterByFields(JsonNode root, Iterable<String> fields) {
    filterNode(createJsonFilterNode(fields), root);
  }

  private static JsonFilterNode createJsonFilterNode(Iterable<String> fields) {
    JsonFilterNode rootFilterNode = new JsonFilterNode();
    for (String field : fields) {
      appendFilterNode(rootFilterNode, field);
    }
    return rootFilterNode;
  }

  private static void appendFilterNode(JsonFilterNode rootFilterNode, String field) {
    JsonFilterNode filterNode = rootFilterNode;
    for (String part : field.split("\\.")) {
      filterNode = filterNode.addOrGet(part);
    }
  }

  private static void filterNode(JsonFilterNode filter, JsonNode node) {
    if (node.isObject()) {
      filterObjectNode(filter, (ObjectNode) node);
    } else if (node.isArray()) {
      filterArrayNode(filter, (ArrayNode) node);
    }
  }

  private static void filterObjectNode(JsonFilterNode filter, ObjectNode objectNode) {
    Iterator<Map.Entry<String,JsonNode>> entryIterator = objectNode.fields();
    while (entryIterator.hasNext()) {
      Map.Entry<String,JsonNode> entry = entryIterator.next();

      JsonFilterNode childFilter = filter.get(entry.getKey());
      if (childFilter == null) {
        entryIterator.remove();
      } else if (!childFilter.isLeaf()) {
        filterNode(childFilter, entry.getValue());
      }
    }
  }

  private static void filterArrayNode(JsonFilterNode filter, ArrayNode arrayNode) {
    for (int i=0; i<arrayNode.size(); i++) {
      filterNode(filter, arrayNode.get(i));
    }
  }

  private static class JsonFilterNode {

    private final Map<String,JsonFilterNode> children = Maps.newHashMap();

    JsonFilterNode addOrGet(String name) {
      JsonFilterNode child = children.get(name);
      if (child == null) {
        child = new JsonFilterNode();
        children.put(name, child);
      }
      return child;
    }

    JsonFilterNode get(String name) {
      return children.get(name);
    }

    boolean isLeaf() {
      return children.isEmpty();
    }
  }

}
