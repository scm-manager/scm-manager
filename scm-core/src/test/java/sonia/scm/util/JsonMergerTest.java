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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class JsonMergerTest {

  private final JsonMerger jsonMerger = new JsonMerger(new ObjectMapper());

  @Test
  void shouldMergeMultipleFieldsWithDto() throws JsonProcessingException {
    JsonNode updateNode = new ObjectMapper().readValue("{ \"name\":\"zorbot\", \"value\":\"42\"}", JsonNode.class);
    TestDto testDto = new TestDto("trillian", "blue", 21, new TestUser(), emptyList());

    TestDto updatedDto = jsonMerger.mergeWithDto(testDto, updateNode);

    assertThat(updatedDto.name).isEqualTo("zorbot");
    assertThat(updatedDto.color).isEqualTo("blue");
    assertThat(updatedDto.value).isEqualTo(42);
  }

  @Test
  void shouldMergeNestedObjects() throws JsonProcessingException {
    JsonNode updateNode = new ObjectMapper().readValue("{ \"user\": { \"username\":\"zorbot\" } }", JsonNode.class);
    TestDto testDto =
      new TestDto("trillian", "blue", 21, new TestUser("trillian", "trillian@hitchhiker.org"), emptyList());

    TestDto updatedDto = jsonMerger.mergeWithDto(testDto, updateNode);

    assertThat(updatedDto.user.username).isEqualTo("zorbot");
    assertThat(updatedDto.user.mail).isEqualTo("trillian@hitchhiker.org");
    assertThat(updatedDto.color).isEqualTo("blue");
    assertThat(updatedDto.value).isEqualTo(21);
  }

  @Test
  void shouldMergeArrays() throws JsonProcessingException {
    JsonNode updateNode = new ObjectMapper().readValue("{ \"fruits\": [\"banana\",\"apple\",\"orange\"] }", JsonNode.class);
    TestDto testDto =
      new TestDto("trillian", "blue", 21, new TestUser(), ImmutableList.of("strawberries"));

    TestDto updatedDto = jsonMerger.mergeWithDto(testDto, updateNode);

    assertThat(updatedDto.fruits).contains("banana", "apple", "orange");
    assertThat(updatedDto.color).isEqualTo("blue");
    assertThat(updatedDto.value).isEqualTo(21);
  }

  @Test
  void shouldRemoveFieldOnNull() throws JsonProcessingException {
    JsonNode updateNode = new ObjectMapper().readValue("{ \"fruits\": null, \"color\":null }", JsonNode.class);

    TestDto testDto =
      new TestDto("trillian", "blue", 21, new TestUser(), ImmutableList.of("strawberries"));
    TestDto updatedDto = jsonMerger.mergeWithDto(testDto, updateNode);

    assertThat(updatedDto.fruits).isNull();
    assertThat(updatedDto.color).isNull();
    assertThat(updatedDto.value).isEqualTo(21);
    assertThat(updatedDto.name).isEqualTo("trillian");
  }

  private static class TestDto {
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
