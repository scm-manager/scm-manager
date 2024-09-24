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

package sonia.scm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class SecureParameterSerializerTest {

  private final SecureParameterSerializer serializer = new SecureParameterSerializer(new ObjectMapper());

  @Test
  void shouldSerializeAndDeserialize() throws IOException {
    TestObject object = new TestObject("1", 2);
    String serialized = serializer.serialize(object);

    assertThat(serialized).isNotEmpty();

    object = serializer.deserialize(serialized, TestObject.class);
    assertThat(object).isNotNull();
    assertThat(object.getOne()).isEqualTo("1");
    assertThat(object.getTwo()).isEqualTo(2);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestObject {

    private String one;
    private int two;

  }


}
