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

package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.otto.edison.hal.HalRepresentation;

import java.util.Iterator;

/**
 * This json merger can be used to apply various new fields to an existing dto without overwriting the whole data.
 *
 * @since 2.17.0
 */
public class JsonMerger {

  private final ObjectMapper objectMapper;

  JsonMerger() {
    objectMapper = new ObjectMapper();
  }

  /**
   * @param dto        dto which should be updated
   * @param updateNode jsonNode {@link JsonNode} which holds the field changes that should be applied
   * @return updated dto
   */
  public <T extends HalRepresentation> T mergeWithDto(T dto, JsonNode updateNode) {
    JsonNode mainNode = objectMapper.convertValue(dto, JsonNode.class);
    JsonNode mergedNode = merge(mainNode, updateNode);

    return (T) objectMapper.convertValue(mergedNode, dto.getClass());
  }

  private JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
    Iterator<String> fieldNames = updateNode.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      if (mainNode.has(fieldName)) {
        ((ObjectNode) mainNode).set(fieldName, updateNode.get(fieldName));
      }
    }
    return mainNode;
  }
}
