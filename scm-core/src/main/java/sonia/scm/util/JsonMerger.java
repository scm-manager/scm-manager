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

package sonia.scm.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import sonia.scm.web.api.DtoValidator;

import javax.inject.Inject;
import java.util.Iterator;

/**
 * This json merger can be used to apply various new fields to an existing object without overwriting
 * the whole data or simply merge some json nodes into one.
 *
 * @since 2.18.0
 */
public class JsonMerger {

  private final ObjectMapper objectMapper;

  @Inject
  public JsonMerger(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Creates a MergerBuilder for the object.
   *
   * @param object object which should be updated
   * @return merger builder
   */
  public MergeStage fromObject(Object object) {
    JsonNode mainNode = objectMapper.valueToTree(object);
    return new MergeStage(objectMapper, mainNode);
  }

  /**
   * Creates a MergerBuilder for the json node.
   *
   * @param mainNode json node which should be updated
   * @return merger builder
   */
  public MergeStage fromJson(JsonNode mainNode) {
    return new MergeStage(objectMapper, mainNode);
  }

  public static class MergeStage {
    private final ObjectMapper objectMapper;
    private final JsonNode mainNode;

    private MergeStage(ObjectMapper objectMapper, JsonNode node) {
      this.objectMapper = objectMapper;
      this.mainNode = node;
    }

    /**
     * Merge object with main node
     *
     * @param object object which will be transformed to a json node in order to be merged with the main node
     * @return this merge builder
     */
    public MergeStage mergeWithObject(Object object) {
      JsonNode updateNode = objectMapper.valueToTree(object);
      merge(mainNode, updateNode);
      return this;
    }

    /**
     * Merge json node with main node
     *
     * @param updateNode json node with data which should be applied to main node
     * @return this merge builder
     */
    public MergeStage mergeWithJson(JsonNode updateNode) {
      merge(mainNode, updateNode);
      return this;
    }

    /**
     * Returns the merged json node
     *
     * @return merged json node
     */
    public JsonNode toJsonNode() {
      return mainNode;
    }

    /**
     * Creates a specific object merger which can apply logic like validation after the actual merge.
     *
     * @param clazz class to which the output should be transformed
     * @return object merger
     */
    public <T> ToObjectStage<T> toObject(Class<T> clazz) {
      return new ToObjectStage<>(objectMapper, mainNode, clazz);
    }

    /**
     * Updates the {@param mainNode} with the provided fields from the {@param updateNode}.
     *
     * @param mainNode   base nodes which will be updated
     * @param updateNode updateNode that contains the new data
     * @return updated json node
     */
    private JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
      Iterator<String> fieldNames = updateNode.fieldNames();
      while (fieldNames.hasNext()) {
        String fieldName = fieldNames.next();
        if (mainNode.has(fieldName)) {
          mergeNodes(mainNode, updateNode, fieldName);
        } else {
          mergeField(mainNode, updateNode, fieldName);
        }
      }
      return mainNode;
    }

    private void mergeField(JsonNode mainNode, JsonNode updateNode, String fieldName) {
      if (mainNode instanceof ObjectNode) {
        JsonNode value = updateNode.get(fieldName);
        if (value.isNull()) {
          return;
        }
        if (value.isIntegralNumber() && value.toString().equals("0")) {
          return;
        }
        if (value.isFloatingPointNumber() && value.toString().equals("0.0")) {
          return;
        }
        ((ObjectNode) mainNode).set(fieldName, value);
      }
    }

    private void mergeNodes(JsonNode mainNode, JsonNode updateNode, String fieldName) {
      JsonNode jsonNode = updateNode.get(fieldName);
      if (jsonNode.isObject()) {
        ((ObjectNode) mainNode).set(fieldName, merge(mainNode.get(fieldName), updateNode.get(fieldName)));
      } else if (jsonNode.isArray()) {
        mergeArray((ObjectNode) mainNode, updateNode, fieldName, jsonNode);
      } else {
        ((ObjectNode) mainNode).set(fieldName, jsonNode);
      }
    }

    private void mergeArray(ObjectNode mainNode, JsonNode updateNode, String fieldName, JsonNode jsonNode) {
      ArrayNode arrayNode = objectMapper.createArrayNode();
      for (int i = 0; i < jsonNode.size(); i++) {
        arrayNode.add(merge(jsonNode.get(i), updateNode.get(fieldName).get(i)));
      }
      mainNode.set(fieldName, arrayNode);
    }
  }

  public static class ToObjectStage<T> {
    private final ObjectMapper objectMapper;
    private final JsonNode node;
    private final Class<T> clazz;
    private boolean shouldValidate = false;

    private ToObjectStage(ObjectMapper objectMapper, JsonNode node, Class<T> clazz) {
      this.objectMapper = objectMapper;
      this.node = node;
      this.clazz = clazz;
    }

    /**
     * Enables validation for dto object
     *
     * @return this builder instance
     */
    public ToObjectStage<T> withValidation() {
      this.shouldValidate = true;
      return this;
    }

    /**
     * Returns the merged object as the provided class
     *
     * @return merged object
     */
    public T build() {
      T updatedObject = objectMapper.convertValue(node, clazz);
      if (shouldValidate) {
        DtoValidator.validate(updatedObject);
      }
      return updatedObject;
    }
  }
}
