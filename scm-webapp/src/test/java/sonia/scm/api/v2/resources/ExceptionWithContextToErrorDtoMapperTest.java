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

package sonia.scm.api.v2.resources;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionWithContextToErrorDtoMapperTest {

  private final ExceptionWithContextToErrorDtoMapper mapper = Mappers.getMapper(ExceptionWithContextToErrorDtoMapper.class);

  @Test
  void shouldMapUrl() {
    ExceptionWithUrl exception = new ExceptionWithUrl();
    ErrorDto dto = mapper.map(exception);
    assertThat(dto.getUrl()).isEqualTo("https://scm-manager.org");
  }

  @Test
  void shouldMapUrlToNull() {
    ExceptionWithoutUrl exception = new ExceptionWithoutUrl();
    ErrorDto dto = mapper.map(exception);
    assertThat(dto.getUrl()).isNull();
  }

  @Test
  void shouldMapAdditionalMessages() {
    ExceptionWithUrl exception = new ExceptionWithUrl();
    ErrorDto dto = mapper.map(exception);
    assertThat(dto.getAdditionalMessages())
      .extracting("message")
      .containsExactly("line 1", "line 2", null);
    assertThat(dto.getAdditionalMessages())
      .extracting("key")
      .containsExactly(null, null, "KEY");
  }

  private static class ExceptionWithUrl extends ExceptionWithContext {
    public ExceptionWithUrl() {
      super(
        ContextEntry.ContextBuilder.noContext(),
        asList(
          new AdditionalMessage(null, "line 1"),
          new AdditionalMessage(null, "line 2"),
          new AdditionalMessage("KEY", null)),
        "With Url");
    }

    @Override
    public String getCode() {
      return "42";
    }

    @Override
    public Optional<String> getUrl() {
      return Optional.of("https://scm-manager.org");
    }
  }

  private static class ExceptionWithoutUrl extends ExceptionWithContext {
    public ExceptionWithoutUrl() {
      super(ContextEntry.ContextBuilder.noContext(), "Without Url");
    }

    @Override
    public String getCode() {
      return "21";
    }
  }

}
