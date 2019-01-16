package sonia.scm.api.v2.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Tag;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TagToTagDtoMapperTest {

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("https://hitchhiker.com"));

  @InjectMocks
  private TagToTagDtoMapperImpl mapper;

  @Test
  void shouldAppendLinks() {
    LinkEnricherRegistry registry = new LinkEnricherRegistry();
    registry.register(Tag.class, (ctx, appender) -> {
      NamespaceAndName repository = ctx.oneRequireByType(NamespaceAndName.class);
      Tag tag = ctx.oneRequireByType(Tag.class);
      appender.appendOne("yo", "http://" + repository.logString() + "/" + tag.getName());
    });
    mapper.setRegistry(registry);

    TagDto dto = mapper.map(new Tag("1.0.0", "42"), new NamespaceAndName("hitchhiker", "hog"));
    assertThat(dto.getLinks().getLinkBy("yo").get().getHref()).isEqualTo("http://hitchhiker/hog/1.0.0");
  }

}
