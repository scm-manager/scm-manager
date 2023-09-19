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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HalAppenderMapperTest {

  @Mock
  private HalAppender appender;

  private HalEnricherRegistry registry;
  private HalAppenderMapper mapper;

  @BeforeEach
  void beforeEach() {
    registry = new HalEnricherRegistry();
    mapper = new HalAppenderMapper();
    mapper.setRegistry(registry);
  }

  @Test
  void shouldAppendSimpleLink() {
    registry.register(String.class, (ctx, appender) -> appender.appendLink("42", "https://hitchhiker.com"));

    mapper.applyEnrichers(appender, "hello");

    verify(appender).appendLink("42", "https://hitchhiker.com");
  }

  @Test
  void shouldCallMultipleEnrichers() {
    registry.register(String.class, (ctx, appender) -> appender.appendLink("42", "https://hitchhiker.com"));
    registry.register(String.class, (ctx, appender) -> appender.appendLink("21", "https://scm.hitchhiker.com"));

    mapper.applyEnrichers(appender, "hello");

    verify(appender).appendLink("42", "https://hitchhiker.com");
    verify(appender).appendLink("21", "https://scm.hitchhiker.com");
  }

  @Test
  void shouldAppendLinkByUsingSourceFromContext() {
    registry.register(String.class, (ctx, appender) -> {
      Optional<String> rel = ctx.oneByType(String.class);
      appender.appendLink(rel.get(), "https://hitchhiker.com");
    });

    mapper.applyEnrichers(appender, "42");

    verify(appender).appendLink("42", "https://hitchhiker.com");
  }

  @Test
  void shouldAppendLinkByUsingMultipleContextEntries() {
    registry.register(Integer.class, (ctx, appender) -> {
      Optional<Integer> rel = ctx.oneByType(Integer.class);
      Optional<String> href = ctx.oneByType(String.class);
      appender.appendLink(String.valueOf(rel.get()), href.get());
    });

    mapper.applyEnrichers(appender, 42, "https://hitchhiker.com");

    verify(appender).appendLink("42", "https://hitchhiker.com");
  }

  @Test
  void shouldHandleExceptionFromEnricher() {
    registry.register(String.class, (ctx, appender) -> {
      throw new RuntimeException("test");
    });

    mapper.applyEnrichers(appender, "hello");

    // no exception has been thrown
  }
}
