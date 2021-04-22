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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.hal.HalRepresentation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonMergerTest {


  @Test
  void shouldMergeSingleNewFieldWithDto() throws JsonProcessingException {
    JsonNode updateNode = new ObjectMapper().readValue("{ \"name\":\"zorbot\"}", JsonNode.class);

    TestDto testDto = new TestDto("trillian", "blue", 21);
    TestDto updatedDto = new JsonMerger().mergeWithDto(testDto, updateNode);

    assertThat(updatedDto.name).isEqualTo("zorbot");
    assertThat(updatedDto.color).isEqualTo("blue");
    assertThat(updatedDto.value).isEqualTo(21);
  }

  @Test
  void shouldMergeMultipleFieldsWithDto() throws JsonProcessingException {
    JsonNode updateNode = new ObjectMapper().readValue("{ \"name\":\"zorbot\", \"value\":\"42\"}", JsonNode.class);

    TestDto testDto = new TestDto("trillian", "blue", 21);
    TestDto updatedDto = new JsonMerger().mergeWithDto(testDto, updateNode);

    assertThat(updatedDto.name).isEqualTo("zorbot");
    assertThat(updatedDto.color).isEqualTo("blue");
    assertThat(updatedDto.value).isEqualTo(42);
  }

  @Test
  void shouldNotMergeFieldsWithDoesNotExistOnDto() throws JsonProcessingException {
    JsonNode updateNode = new ObjectMapper().readValue("{ \"mail\":\"zorbot@hitchhiker.org\", \"color\":\"red\" }", JsonNode.class);

    TestDto testDto = new TestDto("trillian", "blue", 21);
    TestDto updatedDto = new JsonMerger().mergeWithDto(testDto, updateNode);

    assertThat(updatedDto.name).isEqualTo("trillian");
    assertThat(updatedDto.color).isEqualTo("red");
    assertThat(updatedDto.value).isEqualTo(21);
  }

  private static class TestDto extends HalRepresentation {
    String name;
    String color;
    int value;

    public TestDto() {
    }

    public TestDto(String name, String color, int value) {
      this.name = name;
      this.color = color;
      this.value = value;
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
  }
}
