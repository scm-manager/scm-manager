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

package sonia.scm.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class JsonFiltersTest {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testFilterByFields() throws IOException {
    JsonNode node = readJson("filter-test-simple");

    JsonFilters.filterByFields(node, asList("one"));

    assertEquals(1, node.get("one").intValue());
    assertFalse(node.has("two"));
    assertFalse(node.has("three"));
  }

  @Test
  public void testFilterByFieldsWithMultipleFields() throws IOException {
    JsonNode node = readJson("filter-test-simple");

    JsonFilters.filterByFields(node, asList("one", "three"));

    assertEquals(1, node.get("one").intValue());
    assertFalse(node.has("two"));
    assertEquals(3, node.get("three").intValue());
  }

  @Test
  public void testFilterByFieldsWithNonPrimitive() throws IOException {
    JsonNode node = readJson("filter-test-nested");
    JsonFilters.filterByFields(node, asList("two"));
    assertEquals("{\"two\":{\"three\":3,\"four\":4}}", objectMapper.writeValueAsString(node));
  }

  @Test
  public void testFilterByFieldsWithDeepField() throws IOException {
    JsonNode node = readJson("filter-test-nested");
    JsonFilters.filterByFields(node, asList("two.three"));
    assertEquals("{\"two\":{\"three\":3}}", objectMapper.writeValueAsString(node));
  }

  @Test
  public void testFilterByFieldsWithVeryDeepField() throws IOException {
    JsonNode node = readJson("filter-test-deep-path");
    JsonFilters.filterByFields(node, asList("two.three.four.five"));
    assertFalse(node.has("one"));
    String json = objectMapper.writeValueAsString(node.get("two").get("three").get("four").get("five"));
    assertEquals("{\"six\":6,\"seven\":7}", json);
  }

  @Test
  public void testFilterByFieldsWithArray() throws IOException {
    JsonNode node = readJson("filter-test-arrays");
    JsonFilters.filterByFields(node, asList("one.two"));
    ArrayNode one = (ArrayNode) node.get("one");
    assertEquals(2, one.size());
    for (int i=0; i<one.size(); i++) {
      JsonNode childOfOne = one.get(i);
      assertFalse(childOfOne.has("three"));
      assertEquals(2, childOfOne.get("two").intValue());
    }
  }

  private JsonNode readJson(String name) throws IOException {
    URL resource = Resources.getResource("sonia/scm/api/v2/" + name + ".json");
    return objectMapper.readTree(resource);
  }
}
