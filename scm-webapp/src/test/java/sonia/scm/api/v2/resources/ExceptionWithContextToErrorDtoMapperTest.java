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
