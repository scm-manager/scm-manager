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

package sonia.scm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonMergerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final JsonMerger jsonMerger = new JsonMerger(objectMapper);

  @Test
  void shouldMergeMultipleFieldsWithDto() throws JsonProcessingException {
    JsonNode updateNode = objectMapper.readValue("{ \"name\":\"zorbot\", \"value\":\"42\"}", JsonNode.class);
    TestDto testDto = new TestDto("trillian", "blue", 21, new TestUser(), emptyList());

    TestDto updatedDto = jsonMerger.fromObject(testDto).mergeWithJson(updateNode).toObject(TestDto.class).withValidation().build();

    assertThat(updatedDto.name).isEqualTo("zorbot");
    assertThat(updatedDto.color).isEqualTo("blue");
    assertThat(updatedDto.value).isEqualTo(42);
  }

  @Test
  void shouldMergeNestedObjects() throws JsonProcessingException {
    JsonNode updateNode = objectMapper.readValue("{ \"user\": { \"username\":\"zorbot\" } }", JsonNode.class);
    TestDto testDto =
      new TestDto("trillian", "blue", 21, new TestUser("trillian", "trillian@hitchhiker.org"), emptyList());

    TestDto updatedDto = jsonMerger.fromObject(testDto).mergeWithJson(updateNode).toObject(TestDto.class).withValidation().build();

    assertThat(updatedDto.user.username).isEqualTo("zorbot");
    assertThat(updatedDto.user.mail).isEqualTo("trillian@hitchhiker.org");
    assertThat(updatedDto.color).isEqualTo("blue");
    assertThat(updatedDto.value).isEqualTo(21);
  }

  @Test
  void shouldMergeArrays() throws JsonProcessingException {
    JsonNode updateNode = objectMapper.readValue("{ \"fruits\": [\"banana\",\"apple\",\"orange\"] }", JsonNode.class);
    TestDto testDto =
      new TestDto("trillian", "blue", 21, new TestUser(), ImmutableList.of("strawberries"));

    TestDto updatedDto = jsonMerger.fromObject(testDto).mergeWithJson(updateNode).toObject(TestDto.class).withValidation().build();

    assertThat(updatedDto.fruits).contains("banana", "apple", "orange");
    assertThat(updatedDto.color).isEqualTo("blue");
    assertThat(updatedDto.value).isEqualTo(21);
  }

  @Test
  void shouldRemoveFieldOnNull() throws JsonProcessingException {
    JsonNode updateNode = objectMapper.readValue("{ \"fruits\": null, \"color\":null }", JsonNode.class);

    TestDto testDto =
      new TestDto("trillian", "blue", 21, new TestUser(), ImmutableList.of("strawberries"));
    TestDto updatedDto = jsonMerger.fromObject(testDto).mergeWithJson(updateNode).toObject(TestDto.class).withValidation().build();

    assertThat(updatedDto.fruits).isNull();
    assertThat(updatedDto.color).isNull();
    assertThat(updatedDto.value).isEqualTo(21);
    assertThat(updatedDto.name).isEqualTo("trillian");
  }

  @Test
  void shouldMergeJsonNodes() throws JsonProcessingException {
    JsonNode main = objectMapper.readValue(
      "{ \"fruits\": [\"banana\", \"pineapple\"], \"name\":\"main\" }",
      JsonNode.class);
    JsonNode update = objectMapper.readValue("{ \"fruits\": [\"apple\"], \"color\":\"blue\"}", JsonNode.class);

    JsonNode jsonNode = jsonMerger.fromJson(main).mergeWithJson(update).toJsonNode();

    assertThat(jsonNode.get("fruits").toPrettyString()).isEqualTo("[ \"apple\" ]");
    assertThat(jsonNode.get("color").asText()).isEqualTo("blue");
    assertThat(jsonNode.get("name").asText()).isEqualTo("main");
  }

  @Test
  void shouldMergeMultipleTimes() throws JsonProcessingException {
    TestDto mainDto = new TestDto("main", null, 0, new TestUser("trillian", ""), emptyList());
    TestDto firstUpdate = new TestDto("main", "blue", 42, new TestUser("trillian", "trillian@hitchhiker.org"), emptyList());
    JsonNode secondUpdate = objectMapper.readValue("{ \"fruits\": [\"banana\", \"pineapple\"], \"color\":\"red\" }", JsonNode.class);

    TestDto resultDto = jsonMerger
      .fromObject(mainDto)
      .mergeWithObject(firstUpdate)
      .mergeWithJson(secondUpdate)
      .toObject(TestDto.class)
      .withValidation()
      .build();

    assertThat(resultDto.name).isEqualTo("main");
    assertThat(resultDto.color).isEqualTo("red");
    assertThat(resultDto.value).isEqualTo(42);
    assertThat(resultDto.user.username).isEqualTo("trillian");
    assertThat(resultDto.user.mail).isEqualTo("trillian@hitchhiker.org");
    assertThat(resultDto.fruits).contains("banana", "pineapple");
  }

  @Test
  void shouldFailForInvalidDto() {
    TestDto mainDto = new TestDto("main", null, 0, new TestUser("trillian", ""), emptyList());
    TestDto firstUpdate = new TestDto(null, "blue", 42, new TestUser("trillian", "trillian@hitchhiker.org"), emptyList());

    JsonMerger.ToObjectStage<TestDto> objectStage = jsonMerger.fromObject(mainDto)
      .mergeWithObject(firstUpdate)
      .toObject(TestDto.class)
      .withValidation();

    assertThrows(ConstraintViolationException.class, objectStage::build);
  }

  private static class TestDto {
    @NotNull
    String name;
    String color;
    int value;
    TestUser user;
    List<String> fruits;

    public TestDto() {
    }

    public TestDto(String name, String color, int value, TestUser user, List<String> fruits) {
      this.name = name;
      this.color = color;
      this.value = value;
      this.user = user;
      this.fruits = fruits;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getColor() {
      return color;
    }

    public void setColor(String color) {
      this.color = color;
    }

    public int getValue() {
      return value;
    }

    public void setValue(int value) {
      this.value = value;
    }

    public TestUser getUser() {
      return user;
    }

    public void setUser(TestUser user) {
      this.user = user;
    }

    public List<String> getFruits() {
      return fruits;
    }

    public void setFruits(List<String> fruits) {
      this.fruits = fruits;
    }
  }

  private static class TestUser {
    private String username;
    private String mail;

    public TestUser() {
    }

    public TestUser(String username, String mail) {
      this.username = username;
      this.mail = mail;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getMail() {
      return mail;
    }

    public void setMail(String mail) {
      this.mail = mail;
    }
  }
}
