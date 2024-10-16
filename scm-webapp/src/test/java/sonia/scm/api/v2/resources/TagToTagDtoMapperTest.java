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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.Signature;
import sonia.scm.repository.SignatureStatus;
import sonia.scm.repository.Tag;
import sonia.scm.repository.TagGuard;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagToTagDtoMapperTest {

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("https://hitchhiker.com"));
  private final HashSet<TagGuard> tagGuardSet = new HashSet<>();

  private TagToTagDtoMapperImpl mapper;

  @Mock
  private Subject subject;

  @BeforeEach
  void setupSubject() {
    ThreadContext.bind(subject);
  }

  @BeforeEach
  void initMapper() {
    mapper = new TagToTagDtoMapperImpl();
    mapper.setResourceLinks(resourceLinks);
    mapper.setTagGuardSet(tagGuardSet);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldAppendLinks() {
    HalEnricherRegistry registry = new HalEnricherRegistry();
    registry.register(Tag.class, (ctx, appender) -> {
      Repository repository = ctx.oneRequireByType(Repository.class);
      Tag tag = ctx.oneRequireByType(Tag.class);

      appender.appendLink("yo", "http://" + repository.getNamespace() + "/" + repository.getName() + "/" + tag.getName());
    });
    mapper.setRegistry(registry);

    TagDto dto = mapper.map(new Tag("1.0.0", "42"), RepositoryTestData.createHeartOfGold());
    assertThat(dto.getLinks().getLinkBy("yo").get().getHref()).isEqualTo("http://hitchhiker/HeartOfGold/1.0.0");
  }

  @Test
  void shouldMapDate() {
    final long now = Instant.now().getEpochSecond() * 1000;
    TagDto dto = mapper.map(new Tag("1.0.0", "42", now), RepositoryTestData.createHeartOfGold());
    assertThat(dto.getDate()).isEqualTo(Instant.ofEpochMilli(now));
  }

  @Test
  void shouldContainSignatureArray() {
    TagDto dto = mapper.map(new Tag("1.0.0", "42"), RepositoryTestData.createHeartOfGold());
    assertThat(dto.getSignatures()).isNotNull();
  }

  @Test
  void shouldMapSignatures() {
    final Tag tag = new Tag("1.0.0", "42");
    tag.addSignature(new Signature("29v391239v", "gpg", SignatureStatus.VERIFIED, "me", Collections.emptySet()));
    TagDto dto = mapper.map(tag, RepositoryTestData.createHeartOfGold());
    assertThat(dto.getSignatures()).isNotEmpty();
  }

  @Test
  void shouldAddDeleteLinkWithoutGuard() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    when(subject.isPermitted("repository:push:" + repository.getId())).thenReturn(true);
    final Tag tag = new Tag("1.0.0", "42");
    TagDto dto = mapper.map(tag, repository);
    assertThat(dto.getLinks().getLinkBy("delete")).isNotEmpty();
  }

  @Test
  void shouldAddDeleteLinkIfGuardsAreOkay() {
    tagGuardSet.add(deletionRequest -> true);
    tagGuardSet.add(deletionRequest -> true);
    Repository repository = RepositoryTestData.createHeartOfGold();
    when(subject.isPermitted("repository:push:" + repository.getId())).thenReturn(true);
    final Tag tag = new Tag("1.0.0", "42");
    TagDto dto = mapper.map(tag, repository);
    assertThat(dto.getLinks().getLinkBy("delete")).isNotEmpty();
  }

  @Test
  void shouldNotAddDeleteLinkIfGuardInterferes() {
    tagGuardSet.add(deletionRequest -> true);
    tagGuardSet.add(deletionRequest -> false);
    Repository repository = RepositoryTestData.createHeartOfGold();
    when(subject.isPermitted("repository:push:" + repository.getId())).thenReturn(true);
    final Tag tag = new Tag("1.0.0", "42");
    TagDto dto = mapper.map(tag, repository);
    assertThat(dto.getLinks().getLinkBy("delete")).isEmpty();
  }

  @Test
  void shouldNotAddDeleteLinkIfPermissionsAreMissing() {
    final Tag tag = new Tag("1.0.0", "42");
    TagDto dto = mapper.map(tag, RepositoryTestData.createHeartOfGold());
    assertThat(dto.getLinks().getLinkBy("delete")).isEmpty();
  }

  @Test
  void shouldNotAddDeleteLinksForUndeletableTags() {
    final Tag tag = new Tag("1.0.0", "42", null, false);
    TagDto dto = mapper.map(tag, RepositoryTestData.createHeartOfGold());
    assertThat(dto.getLinks().getLinkBy("delete")).isEmpty();
  }

}
