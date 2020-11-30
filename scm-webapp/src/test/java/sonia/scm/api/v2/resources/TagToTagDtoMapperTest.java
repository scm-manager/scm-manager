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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.Signature;
import sonia.scm.repository.SignatureStatus;
import sonia.scm.repository.Tag;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagToTagDtoMapperTest {

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("https://hitchhiker.com"));

  @InjectMocks
  private TagToTagDtoMapperImpl mapper;

  @Mock
  private Subject subject;

  @BeforeEach
  void setupSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldAppendLinks() {
    HalEnricherRegistry registry = new HalEnricherRegistry();
    registry.register(Tag.class, (ctx, appender) -> {
      NamespaceAndName repository = ctx.oneRequireByType(NamespaceAndName.class);
      Tag tag = ctx.oneRequireByType(Tag.class);
      appender.appendLink("yo", "http://" + repository.logString() + "/" + tag.getName());
    });
    mapper.setRegistry(registry);

    TagDto dto = mapper.map(new Tag("1.0.0", "42"), new NamespaceAndName("hitchhiker", "hog"), RepositoryTestData.createHeartOfGold());
    assertThat(dto.getLinks().getLinkBy("yo").get().getHref()).isEqualTo("http://hitchhiker/hog/1.0.0");
  }

  @Test
  void shouldMapDate() {
    final long now = Instant.now().getEpochSecond() * 1000;
    TagDto dto = mapper.map(new Tag("1.0.0", "42", now), new NamespaceAndName("hitchhiker", "hog"), RepositoryTestData.createHeartOfGold());
    assertThat(dto.getDate()).isEqualTo(Instant.ofEpochMilli(now));
  }

  @Test
  void shouldContainSignatureArray() {
    TagDto dto = mapper.map(new Tag("1.0.0", "42"), new NamespaceAndName("hitchhiker", "hog"), RepositoryTestData.createHeartOfGold());
    assertThat(dto.getSignatures()).isNotNull();
  }

  @Test
  void shouldMapSignatures() {
    final Tag tag = new Tag("1.0.0", "42");
    tag.addSignature(new Signature("29v391239v", "gpg", SignatureStatus.VERIFIED, "me", Collections.emptySet()));
    TagDto dto = mapper.map(tag, new NamespaceAndName("hitchhiker", "hog"), RepositoryTestData.createHeartOfGold());
    assertThat(dto.getSignatures()).isNotEmpty();
  }

  @Test
  void shouldAddDeleteLink() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    when(subject.isPermitted("repository:push:" + repository.getId())).thenReturn(true);
    final Tag tag = new Tag("1.0.0", "42");
    TagDto dto = mapper.map(tag, new NamespaceAndName(repository.getNamespace(), repository.getName()), repository);
    assertThat(dto.getLinks().getLinkBy("delete")).isNotEmpty();
  }

  @Test
  void shouldNotAddDeleteLinkIfPermissionsAreMissing() {
    final Tag tag = new Tag("1.0.0", "42");
    TagDto dto = mapper.map(tag, new NamespaceAndName("hitchhiker", "hog"), RepositoryTestData.createHeartOfGold());
    assertThat(dto.getLinks().getLinkBy("delete")).isEmpty();
  }

  @Test
  void shouldNotAddDeleteLinksForUndeletableTags() {
    final Tag tag = new Tag("1.0.0", "42", null, false);
    TagDto dto = mapper.map(tag, new NamespaceAndName("hitchhiker", "hog"), RepositoryTestData.createHeartOfGold());
    assertThat(dto.getLinks().getLinkBy("delete")).isEmpty();
  }

}
