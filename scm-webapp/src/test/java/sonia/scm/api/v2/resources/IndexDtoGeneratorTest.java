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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.BasicContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.initialization.InitializationFinisher;
import sonia.scm.initialization.InitializationStep;
import sonia.scm.initialization.InitializationStepResource;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SearchableType;
import sonia.scm.security.AnonymousMode;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static de.otto.edison.hal.Link.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.SCMContext.USER_ANONYMOUS;

@ExtendWith(MockitoExtension.class)
class IndexDtoGeneratorTest {

  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("/api/v2"));
  @Mock
  private BasicContextProvider contextProvider;
  @Mock
  private ScmConfiguration configuration;
  @Mock
  private InitializationFinisher initializationFinisher;
  @Mock
  private SearchEngine searchEngine;
  private IndexDtoGenerator generator;

  @BeforeEach
  void setUp() {
    generator = new IndexDtoGenerator(resourceLinks, contextProvider, configuration, initializationFinisher, searchEngine, false);
  }

  @Nested
  class WithFullyInitializedSystem {

    @Mock
    private Subject subject;

    @BeforeEach
    void fullyInitialized() {
      when(initializationFinisher.isFullyInitialized()).thenReturn(true);
    }

    @BeforeEach
    void bindSubject() {
      ThreadContext.bind(subject);
    }

    @AfterEach
    void tearDownSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldAppendMeIfAuthenticated() {
      when(subject.isAuthenticated()).thenReturn(true);

      when(contextProvider.getVersion()).thenReturn("2.x");

      IndexDto dto = generator.generate();

      assertThat(dto.getLinks().getLinkBy("me")).isPresent();
    }

    @Test
    void shouldNotAppendMeIfUserIsAuthenticatedButAnonymous() {
      when(subject.getPrincipal()).thenReturn(USER_ANONYMOUS);
      when(subject.isAuthenticated()).thenReturn(true);

      IndexDto dto = generator.generate();

      assertThat(dto.getLinks().getLinkBy("me")).isNotPresent();
    }

    @Test
    void shouldAppendMeIfUserIsAnonymousAndAnonymousModeIsFullEnabled() {
      when(subject.getPrincipal()).thenReturn(USER_ANONYMOUS);
      when(subject.isAuthenticated()).thenReturn(true);
      when(configuration.getAnonymousMode()).thenReturn(AnonymousMode.FULL);

      IndexDto dto = generator.generate();

      assertThat(dto.getLinks().getLinkBy("me")).isPresent();
    }

    @Test
    void shouldNotAppendMeIfUserIsAnonymousAndAnonymousModeIsProtocolOnly() {
      when(subject.getPrincipal()).thenReturn(USER_ANONYMOUS);
      when(subject.isAuthenticated()).thenReturn(true);
      when(configuration.getAnonymousMode()).thenReturn(AnonymousMode.PROTOCOL_ONLY);

      IndexDto dto = generator.generate();

      assertThat(dto.getLinks().getLinkBy("me")).isNotPresent();
    }

    @Test
    void shouldAppendSearchLinksForEveryType() {
      List<SearchableType> types = Arrays.asList(
        searchableType("repository"),
        searchableType("user"),
        searchableType("group")
      );
      when(searchEngine.getSearchableTypes()).thenReturn(types);

      when(subject.isAuthenticated()).thenReturn(true);

      IndexDto dto = generator.generate();
      assertThat(dto.getLinks().getLinksBy("search")).contains(
        Link.linkBuilder("search", "/api/v2/search/query/repository").withName("repository").build(),
        Link.linkBuilder("search", "/api/v2/search/query/user").withName("user").build(),
        Link.linkBuilder("search", "/api/v2/search/query/group").withName("group").build()
      );
    }

    @Nested
    class InvalidationLinks {
      @Test
      void shouldAppendInvalidationLinks() {
        when(subject.isAuthenticated()).thenReturn(true);
        when(subject.isPermitted("configuration:list")).thenReturn(true);
        when(subject.isPermitted("configuration:write:1")).thenReturn(true);
        mockOtherPermissions();
        when(configuration.getId()).thenReturn("1");

        IndexDto dto = generator.generate();
        assertThat(dto.getLinks().getLinkBy("invalidateCaches")).contains(
          Link.linkBuilder("invalidateCaches", "/api/v2/invalidations/caches").build()
        );
        assertThat(dto.getLinks().getLinkBy("invalidateSearchIndex")).contains(
          Link.linkBuilder("invalidateSearchIndex", "/api/v2/invalidations/search-index").build()
        );
      }

      @Test
      void shouldNotAppendInvalidationsIfWritePermissionIsMissing() {
        when(subject.isAuthenticated()).thenReturn(true);
        when(subject.isPermitted("configuration:list")).thenReturn(true);
        when(subject.isPermitted("configuration:write:1")).thenReturn(false);
        mockOtherPermissions();
        when(configuration.getId()).thenReturn("1");

        IndexDto dto = generator.generate();
        assertThat(dto.getLinks().getLinkBy("invalidateCaches")).isEmpty();
        assertThat(dto.getLinks().getLinkBy("invalidateSearchIndex")).isEmpty();
      }

      @Test
      void shouldNotAppendInvalidationsIfListPermissionIsMissing() {
        when(subject.isAuthenticated()).thenReturn(true);
        when(subject.isPermitted("configuration:list")).thenReturn(false);
        mockOtherPermissions();

        IndexDto dto = generator.generate();
        assertThat(dto.getLinks().getLinkBy("invalidateCaches")).isEmpty();
        assertThat(dto.getLinks().getLinkBy("invalidateSearchIndex")).isEmpty();
      }

      @Test
      void shouldNotAppendInvalidationsIfUnauthenticated() {
        when(subject.isAuthenticated()).thenReturn(false);

        IndexDto dto = generator.generate();
        assertThat(dto.getLinks().getLinkBy("invalidateCaches")).isEmpty();
        assertThat(dto.getLinks().getLinkBy("invalidateSearchIndex")).isEmpty();
      }

      private void mockOtherPermissions() {
        when(subject.isPermitted("plugin:read")).thenReturn(false);
        when(subject.isPermitted("plugin:write")).thenReturn(false);
        when(subject.isPermitted("user:list")).thenReturn(false);
        when(subject.isPermitted("user:autocomplete")).thenReturn(false);
        when(subject.isPermitted("group:autocomplete")).thenReturn(false);
        when(subject.isPermitted("group:list")).thenReturn(false);
      }
    }
  }

  private SearchableType searchableType(String name) {
    SearchableType searchableType = mock(SearchableType.class);
    when(searchableType.getName()).thenReturn(name);
    return searchableType;
  }

  @Nested
  class WithUnfinishedInitialization {

    @Mock
    private InitializationStep initializationStep;
    @Mock
    private InitializationStepResource initializationStepResource;

    @Test
    void shouldCreateInitializationLink() {
      when(initializationFinisher.isFullyInitialized()).thenReturn(false);
      when(initializationFinisher.missingInitialization()).thenReturn(initializationStep);
      when(initializationStep.name()).thenReturn("probability");
      when(initializationFinisher.getResource("probability")).thenReturn(initializationStepResource);

      doAnswer(invocationOnMock -> {
        Links.Builder initializationLinkBuilder = invocationOnMock.getArgument(0, Links.Builder.class);
        Embedded.Builder initializationEmbeddedBuilder = invocationOnMock.getArgument(1, Embedded.Builder.class);
        initializationLinkBuilder.single(link("init", "/init"));
        return null;
      }).when(initializationStepResource).setupIndex(any(), any(), any());

      IndexDto dto = generator.generate();

      assertThat(dto.getInitialization()).isEqualTo("probability");
      List<InitializationDto> initializationDtos = dto.getEmbedded().getItemsBy("probability", InitializationDto.class);
      assertThat(initializationDtos).hasSize(1).allMatch(
        initializationDto -> {
          assertThat(initializationDto.getLinks().hasLink("init")).isTrue();
          return true;
        }
      );
    }
  }
}
