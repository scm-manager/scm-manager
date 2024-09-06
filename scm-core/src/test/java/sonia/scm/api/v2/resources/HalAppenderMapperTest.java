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
