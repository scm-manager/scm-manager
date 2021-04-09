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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.BasicContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.initialization.InitializationFinisher;
import sonia.scm.initialization.InitializationStep;
import sonia.scm.initialization.InitializationStepResource;
import sonia.scm.security.AnonymousMode;

import java.net.URI;
import java.util.List;

import static de.otto.edison.hal.Link.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static sonia.scm.SCMContext.USER_ANONYMOUS;

@ExtendWith(MockitoExtension.class)
class IndexDtoGeneratorTest {

  private static final ScmPathInfo scmPathInfo = () -> URI.create("/api/v2");

  @Mock
  private ResourceLinks resourceLinks;
  @Mock
  private BasicContextProvider contextProvider;
  @Mock
  private ScmConfiguration configuration;
  @Mock
  private InitializationFinisher initializationFinisher;

  @InjectMocks
  private IndexDtoGenerator generator;

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
      mockSubjectRelatedResourceLinks();
      when(subject.isAuthenticated()).thenReturn(true);

      when(contextProvider.getVersion()).thenReturn("2.x");

      IndexDto dto = generator.generate();

      assertThat(dto.getLinks().getLinkBy("me")).isPresent();
    }

    @Test
    void shouldNotAppendMeIfUserIsAuthenticatedButAnonymous() {
      mockResourceLinks();
      when(subject.getPrincipal()).thenReturn(USER_ANONYMOUS);
      when(subject.isAuthenticated()).thenReturn(true);

      IndexDto dto = generator.generate();

      assertThat(dto.getLinks().getLinkBy("me")).isNotPresent();
    }

    @Test
    void shouldAppendMeIfUserIsAnonymousAndAnonymousModeIsFullEnabled() {
      mockSubjectRelatedResourceLinks();
      when(subject.getPrincipal()).thenReturn(USER_ANONYMOUS);
      when(subject.isAuthenticated()).thenReturn(true);
      when(configuration.getAnonymousMode()).thenReturn(AnonymousMode.FULL);

      IndexDto dto = generator.generate();

      assertThat(dto.getLinks().getLinkBy("me")).isPresent();
    }

    @Test
    void shouldNotAppendMeIfUserIsAnonymousAndAnonymousModeIsProtocolOnly() {
      mockResourceLinks();
      when(subject.getPrincipal()).thenReturn(USER_ANONYMOUS);
      when(subject.isAuthenticated()).thenReturn(true);
      when(configuration.getAnonymousMode()).thenReturn(AnonymousMode.PROTOCOL_ONLY);

      IndexDto dto = generator.generate();

      assertThat(dto.getLinks().getLinkBy("me")).isNotPresent();
    }
  }

  @Nested
  class WithUnfinishedInitialization {

    @Mock
    private InitializationStep initializationStep;
    @Mock
    private InitializationStepResource initializationStepResource;

    @Test
    void shouldCreateInitializationLink() {
      mockBaseLink();
      when(initializationFinisher.isFullyInitialized()).thenReturn(false);
      when(initializationFinisher.missingInitialization()).thenReturn(initializationStep);
      when(initializationStep.name()).thenReturn("probability");
      when(initializationFinisher.getResource("probability")).thenReturn(initializationStepResource);

      doAnswer(invocationOnMock -> {
        Links.Builder initializationLinkBuilder = invocationOnMock.getArgument(0, Links.Builder.class);
        Embedded.Builder initializationEmbeddedBuilder = invocationOnMock.getArgument(1, Embedded.Builder.class);
        initializationLinkBuilder.single(link("init", "/init"));
        return null;
      }).when(initializationStepResource).setupIndex(any(), any());

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

  private void mockResourceLinks() {
    mockBaseLink();
    when(resourceLinks.authentication()).thenReturn(new ResourceLinks.AuthenticationLinks(scmPathInfo));
  }

  private void mockBaseLink() {
    when(resourceLinks.index()).thenReturn(new ResourceLinks.IndexLinks(scmPathInfo));
    when(resourceLinks.uiPluginCollection()).thenReturn(new ResourceLinks.UIPluginCollectionLinks(scmPathInfo));
  }

  private void mockSubjectRelatedResourceLinks() {
    mockResourceLinks();
    when(resourceLinks.repositoryCollection()).thenReturn(new ResourceLinks.RepositoryCollectionLinks(scmPathInfo));
    when(resourceLinks.repositoryVerbs()).thenReturn(new ResourceLinks.RepositoryVerbLinks(scmPathInfo));
    when(resourceLinks.repositoryTypeCollection()).thenReturn(new ResourceLinks.RepositoryTypeCollectionLinks(scmPathInfo));
    when(resourceLinks.repositoryRoleCollection()).thenReturn(new ResourceLinks.RepositoryRoleCollectionLinks(scmPathInfo));
    when(resourceLinks.namespaceStrategies()).thenReturn(new ResourceLinks.NamespaceStrategiesLinks(scmPathInfo));
    when(resourceLinks.namespaceCollection()).thenReturn(new ResourceLinks.NamespaceCollectionLinks(scmPathInfo));
    when(resourceLinks.me()).thenReturn(new ResourceLinks.MeLinks(scmPathInfo, new ResourceLinks.UserLinks(scmPathInfo)));
    when(resourceLinks.repository()).thenReturn(new ResourceLinks.RepositoryLinks(scmPathInfo));
  }
}
