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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.otto.edison.hal.HalRepresentation;
import org.assertj.core.data.Index;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SubjectAware("slarti")
@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class RepositoryTypeToRepositoryTypeDtoMapperTest {

  private final URI baseUri = URI.create("https://scm-manager.org/scm/");

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private HalEnricherRegistry registry;

  @InjectMocks
  private RepositoryTypeToRepositoryTypeDtoMapperImpl mapper;

  private final RepositoryType type = new RepositoryType("hk", "Hitchhiker", Sets.newHashSet());

  @Test
  void shouldMapSimpleProperties() {
    RepositoryTypeDto dto = mapper.map(type);
    assertThat(dto.getName()).isEqualTo("hk");
    assertThat(dto.getDisplayName()).isEqualTo("Hitchhiker");
  }

  @Test
  void shouldAppendSelfLink() {
    RepositoryTypeDto dto = mapper.map(type);
    assertLink(dto, "self", "https://scm-manager.org/scm/v2/repositoryTypes/hk");
  }

  private void assertLink(RepositoryTypeDto dto, String rel, String href) {
    assertThat(dto.getLinks().getLinkBy(rel)).hasValueSatisfying(
      link -> assertThat(link.getHref()).isEqualTo(href)
    );
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "repository:create")
  void shouldAppendImportFromUrlLink() {
    RepositoryType type = new RepositoryType("hk", "Hitchhiker", ImmutableSet.of(Command.PULL));

    RepositoryTypeDto dto = mapper.map(type);
    assertLink(dto, "import", "https://scm-manager.org/scm/v2/repositories/import/hk/url");
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "repository:create")
  void shouldNotAppendImportFromUrlLinkIfCommandNotSupported() {
    RepositoryTypeDto dto = mapper.map(type);
    assertThat(dto.getLinks().getLinkBy("import")).isEmpty();
  }

  @Test
  void shouldNotAppendImportFromUrlLinkIfNotPermitted() {
    RepositoryType type = new RepositoryType("hk", "Hitchhiker", ImmutableSet.of(Command.PULL));

    RepositoryTypeDto dto = mapper.map(type);
    assertThat(dto.getLinks().getLinkBy("import")).isEmpty();
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "repository:create")
  void shouldAppendImportFromBundleLinkAndFullImportLink() {
    RepositoryType type = new RepositoryType("hk", "Hitchhiker", ImmutableSet.of(Command.UNBUNDLE));

    RepositoryTypeDto dto = mapper.map(type);

    assertThat(dto.getLinks().getLinksBy("import"))
      .hasSize(2)
      .satisfies(link -> {
          assertThat(link.getHref()).isEqualTo("https://scm-manager.org/scm/v2/repositories/import/hk/bundle");
        }, Index.atIndex(0)
      )
      .satisfies(link -> {
          assertThat(link.getHref()).isEqualTo("https://scm-manager.org/scm/v2/repositories/import/hk/full");
        }, Index.atIndex(1)
      );
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "repository:create")
  void shouldNotAppendImportFromBundleLinkOrFullImportLinkIfCommandNotSupported() {
    RepositoryTypeDto dto = mapper.map(type);
    assertThat(dto.getLinks().getLinkBy("import")).isEmpty();
  }

  @Test
  void shouldNotAppendImportFromBundleLinkIfNotPermitted() {
    RepositoryType type = new RepositoryType("hk", "Hitchhiker", ImmutableSet.of(Command.UNBUNDLE));

    RepositoryTypeDto dto = mapper.map(type);
    assertThat(dto.getLinks().getLinkBy("import")).isEmpty();
  }

  @Test
  void shouldEnrichLinks() {
    when(registry.allByType(RepositoryType.class)).thenReturn(Collections.singleton(new LinkEnricher()));

    RepositoryTypeDto dto = mapper.map(type);
    assertLink(dto, "spaceship", "/spaceships/heart-of-gold");
  }

  @Test
  void shouldEnrichEmbedded() {
    when(registry.allByType(RepositoryType.class)).thenReturn(Collections.singleton(new EmbeddedEnricher()));

    RepositoryTypeDto dto = mapper.map(type);
    assertThat(dto.getEmbedded().getItemsBy("spaceship")).hasSize(1);
  }

  private static class LinkEnricher implements HalEnricher {

    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {
      appender.appendLink("spaceship", "/spaceships/heart-of-gold");
    }
  }

  private static class EmbeddedEnricher implements HalEnricher {

    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {
      appender.appendEmbedded("spaceship", new HalRepresentation());
    }
  }
}
