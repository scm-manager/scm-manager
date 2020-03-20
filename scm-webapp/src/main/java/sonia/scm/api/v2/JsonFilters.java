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
    
package sonia.scm.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static java.util.Arrays.stream;

final class JsonFilters {

  private JsonFilters() {
  }

  static void filterByFields(JsonNode root, Collection<String> filterExpressions) {
    createJsonFilterNode(filterExpressions).filterNode(root);
  }

  private static JsonFilterNode createJsonFilterNode(Collection<String> filterExpressions) {
    JsonFilterNode rootFilterNode = new JsonFilterNode();
    filterExpressions.stream()
      .map(JsonFilterNode::expressionPartIterator)
      .forEach(rootFilterNode::appendFilterExpression);
    return rootFilterNode;
  }

  private static class JsonFilterNode {

    private final Map<String,JsonFilterNode> children = Maps.newHashMap();

    private static Iterator<String> expressionPartIterator(String filterExpression) {
      return stream(filterExpression.split("\\.")).iterator();
    }

    private void appendFilterExpression(Iterator<String> fields) {
      if (fields.hasNext()) {
        addOrGet(fields.next()).appendFilterExpression(fields);
      }
    }

    private void filterNode(JsonNode node) {
      if (!isLeaf()) {
        if (node.isObject()) {
          filterObjectNode((ObjectNode) node);
        } else if (node.isArray()) {
          filterArrayNode((ArrayNode) node);
        }
      }
    }

    private void filterObjectNode(ObjectNode objectNode) {
      Iterator<Map.Entry<String,JsonNode>> entryIterator = objectNode.fields();
      while (entryIterator.hasNext()) {
        Map.Entry<String,JsonNode> entry = entryIterator.next();

        JsonFilterNode childFilter = get(entry.getKey());
        if (childFilter == null) {
          entryIterator.remove();
        } else {
          childFilter.filterNode(entry.getValue());
        }
      }
    }

    private void filterArrayNode(ArrayNode arrayNode) {
      arrayNode.forEach(this::filterNode);
    }

    private JsonFilterNode addOrGet(String name) {
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
