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
 * This json merger can be used to apply various new fields to an existing dto without overwriting the whole data.
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
   * Updates the provided dto with the data from the json node
   *
   * @param dto        dto which should be updated
   * @param updateNode jsonNode {@link JsonNode} which holds the field changes that should be applied
   * @return updated dto
   */
  public <T> T mergeWithDto(T dto, JsonNode updateNode) {
    return mergeWithDto(dto, updateNode, true);
  }

  /**
   * Updates the provided dto with the data from the json node
   *
   * @param dto            dto which should be updated
   * @param updateNode     jsonNode {@link JsonNode} which holds the field changes that should be applied
   * @param shouldValidate validates the dto if {@code true}
   * @return updated dto
   */
  public <T> T mergeWithDto(T dto, JsonNode updateNode, boolean shouldValidate) {
    JsonNode mainNode = objectMapper.valueToTree(dto);
    JsonNode mergedNode = merge(mainNode, updateNode);

    T updatedDto = (T) objectMapper.convertValue(mergedNode, dto.getClass());
    if (shouldValidate) {
      DtoValidator.validate(updatedDto);
    }
    return updatedDto;
  }

  /**
   * Updates the {@param mainNode} with the provided fields from the {@param updateNode}.
   *
   * @param mainNode base nodes which will be updated
   * @param updateNode
   * @return updated json node
   */
  public JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
    Iterator<String> fieldNames = updateNode.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      if (mainNode.has(fieldName)) {
        mergeNodes(mainNode, updateNode, fieldName);
      } else {
        ((ObjectNode) mainNode).set(fieldName, updateNode);
      }
    }
    return mainNode;
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
