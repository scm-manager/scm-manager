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

    mapper.appendLinks(appender, "hello");

    verify(appender).appendLink("42", "https://hitchhiker.com");
  }

  @Test
  void shouldCallMultipleEnrichers() {
    registry.register(String.class, (ctx, appender) -> appender.appendLink("42", "https://hitchhiker.com"));
    registry.register(String.class, (ctx, appender) -> appender.appendLink("21", "https://scm.hitchhiker.com"));

    mapper.appendLinks(appender, "hello");

    verify(appender).appendLink("42", "https://hitchhiker.com");
    verify(appender).appendLink("21", "https://scm.hitchhiker.com");
  }

  @Test
  void shouldAppendLinkByUsingSourceFromContext() {
    registry.register(String.class, (ctx, appender) -> {
      Optional<String> rel = ctx.oneByType(String.class);
      appender.appendLink(rel.get(), "https://hitchhiker.com");
    });

    mapper.appendLinks(appender, "42");

    verify(appender).appendLink("42", "https://hitchhiker.com");
  }

  @Test
  void shouldAppendLinkByUsingMultipleContextEntries() {
    registry.register(Integer.class, (ctx, appender) -> {
      Optional<Integer> rel = ctx.oneByType(Integer.class);
      Optional<String> href = ctx.oneByType(String.class);
      appender.appendLink(String.valueOf(rel.get()), href.get());
    });

    mapper.appendLinks(appender, Integer.valueOf(42), "https://hitchhiker.com");

    verify(appender).appendLink("42", "https://hitchhiker.com");
  }

}
