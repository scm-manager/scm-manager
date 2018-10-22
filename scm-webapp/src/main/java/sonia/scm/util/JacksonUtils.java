package sonia.scm.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;

public class JacksonUtils {

  private JacksonUtils() {
  }

  public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
    Iterator<String> fieldNames = updateNode.fieldNames();

    while (fieldNames.hasNext()) {

      String fieldName = fieldNames.next();
      JsonNode jsonNode = mainNode.get(fieldName);

      if (jsonNode != null) {
        if (jsonNode.isObject()) {
          merge(jsonNode, updateNode.get(fieldName));
        } else if (jsonNode.isArray()) {
          for (int i = 0; i < jsonNode.size(); i++) {
            merge(jsonNode.get(i), updateNode.get(fieldName).get(i));
          }
        }
      } else {
        if (mainNode instanceof ObjectNode) {
          // Overwrite field
          JsonNode value = updateNode.get(fieldName);
          if (value.isNull()) {
            continue;
          }
          if (value.isIntegralNumber() && value.toString().equals("0")) {
            continue;
          }
          if (value.isFloatingPointNumber() && value.toString().equals("0.0")) {
            continue;
          }
          ((ObjectNode) mainNode).put(fieldName, value);
        }
      }
    }

    return mainNode;
  }
}
