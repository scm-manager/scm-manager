package sonia.scm.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class JsonFiltersTest {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testFilterByFields() throws IOException {
    JsonNode node = readJson("filter-test-001");

    JsonFilters.filterByFields(node, Lists.newArrayList("one"));

    assertEquals(1, node.get("one").intValue());
    assertFalse(node.has("two"));
    assertFalse(node.has("three"));
  }

  @Test
  public void testFilterByFieldsWithMultipleFields() throws IOException {
    JsonNode node = readJson("filter-test-001");

    JsonFilters.filterByFields(node, Lists.newArrayList("one", "three"));

    assertEquals(1, node.get("one").intValue());
    assertFalse(node.has("two"));
    assertEquals(3, node.get("three").intValue());
  }

  @Test
  public void testFilterByFieldsWithNonPrimitive() throws IOException {
    JsonNode node = readJson("filter-test-002");
    JsonFilters.filterByFields(node, Lists.newArrayList("two"));
    assertEquals("{\"two\":{\"three\":3,\"four\":4}}", objectMapper.writeValueAsString(node));
  }

  @Test
  public void testFilterByFieldsWithDeepField() throws IOException {
    JsonNode node = readJson("filter-test-002");
    JsonFilters.filterByFields(node, Lists.newArrayList("two.three"));
    assertEquals("{\"two\":{\"three\":3}}", objectMapper.writeValueAsString(node));
  }

  @Test
  public void testFilterByFieldsWithVeryDeepField() throws IOException {
    JsonNode node = readJson("filter-test-003");
    JsonFilters.filterByFields(node, Lists.newArrayList("two.three.four.five"));
    assertFalse(node.has("one"));
    String json = objectMapper.writeValueAsString(node.get("two").get("three").get("four").get("five"));
    assertEquals("{\"six\":6,\"seven\":7}", json);
  }

  @Test
  public void testFilterByFieldsWithArray() throws IOException {
    JsonNode node = readJson("filter-test-004");
    JsonFilters.filterByFields(node, Lists.newArrayList("one.two"));
    ArrayNode one = (ArrayNode) node.get("one");
    assertEquals(one.size(), 2);
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
