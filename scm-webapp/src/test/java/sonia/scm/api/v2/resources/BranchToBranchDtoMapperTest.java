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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Branch;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchToBranchDtoMapperTest {

  public static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private final URI baseUri = URI.create("https://hitchhiker.com/api/");

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private Subject subject;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;

  @InjectMocks
  private BranchToBranchDtoMapperImpl mapper;

  @BeforeEach
  void setupSubject() {
    ThreadContext.bind(subject);
  }

  @BeforeEach
  void initService() {
    when(serviceFactory.create(REPOSITORY)).thenReturn(service);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldAppendLinks() {
    HalEnricherRegistry registry = new HalEnricherRegistry();
    registry.register(Branch.class, (ctx, appender) -> {
      NamespaceAndName namespaceAndName = ctx.oneRequireByType(NamespaceAndName.class);
      Branch branch = ctx.oneRequireByType(Branch.class);

      appender.appendLink("ka", "http://" + namespaceAndName.logString() + "/" + branch.getName());
    });
    mapper.setRegistry(registry);

    Branch branch = Branch.normalBranch("master", "42");

    BranchDto dto = mapper.map(branch, REPOSITORY);
    assertThat(dto.getLinks().getLinkBy("ka").get().getHref()).isEqualTo("http://hitchhiker/HeartOfGold/master");
  }

  @Nested
  class WithPushPermissions {

    @BeforeEach
    void setPermission() {
      lenient().when(subject.isPermitted("repository:push:" + REPOSITORY.getId())).thenReturn(true);
    }

    @Test
    void shouldAppendDefaultLink() {
      Branch branch = Branch.normalBranch("master", "42");

      BranchDto dto = mapper.map(branch, REPOSITORY);
      assertThat(dto.getLinks().getLinkBy("history")).isNotEmpty();
      assertThat(dto.getLinks().getLinkBy("changeset")).isNotEmpty();
      assertThat(dto.getLinks().getLinkBy("source")).isNotEmpty();
    }

    @Test
    void shouldAppendDeleteLink() {
      Branch branch = Branch.normalBranch("master", "42");

      BranchDto dto = mapper.map(branch, REPOSITORY);
      assertThat(dto.getLinks().getLinkBy("delete").get().getHref()).isEqualTo("https://hitchhiker.com/api/v2/repositories/hitchhiker/HeartOfGold/branches/master");
    }

    @Test
    void shouldNotAppendDeleteLinkIfDefaultBranch() {
      Branch branch = Branch.defaultBranch("master", "42");

      BranchDto dto = mapper.map(branch, REPOSITORY);
      assertThat(dto.getLinks().getLinkBy("delete")).isNotPresent();
    }

    @Test
    void shouldAppendDetailsLinkIfDetailsCommandIsSupported() {
      Branch branch = Branch.normalBranch("master", "42");
      when(service.isSupported(Command.BRANCH_DETAILS)).thenReturn(true);

      BranchDto dto = mapper.map(branch, REPOSITORY);
      assertThat(dto.getLinks().getLinkBy("details")).isNotEmpty();
    }

    @Test
    void shouldNotAppendDetailsLinkIfDetailsCommandIsNotSupported() {
      Branch branch = Branch.normalBranch("master", "42");
      when(service.isSupported(Command.BRANCH_DETAILS)).thenReturn(false);

      BranchDto dto = mapper.map(branch, REPOSITORY);
      assertThat(dto.getLinks().getLinkBy("details")).isNotPresent();
    }
  }

  @Test
  void shouldNotAppendDeleteLinkIfNotPermitted() {
    when(subject.isPermitted("repository:push:" + REPOSITORY.getId())).thenReturn(false);
    Branch branch = Branch.normalBranch("master", "42");

    BranchDto dto = mapper.map(branch, REPOSITORY);
    assertThat(dto.getLinks().getLinkBy("delete")).isNotPresent();
  }
}
