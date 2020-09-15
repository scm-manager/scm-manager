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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PersonTestData;
import sonia.scm.repository.api.LogCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchToBranchDtoMapperTest {

  private final URI baseUri = URI.create("https://hitchhiker.com");

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService repositoryService;
  @Mock(answer = Answers.RETURNS_SELF)
  private LogCommandBuilder logCommandBuilder;

  @InjectMocks
  private BranchToBranchDtoMapperImpl mapper;

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

    BranchDto dto = mapper.map(branch, new NamespaceAndName("hitchhiker", "heart-of-gold"));
    assertThat(dto.getLinks().getLinkBy("ka").get().getHref()).isEqualTo("http://hitchhiker/heart-of-gold/master");
  }

  @Test
  void shouldMapLastChangeDateAndLastModifier() throws IOException {
    long creationTime = 1000000000;
    Changeset changeset = new Changeset("1", 1L, PersonTestData.ZAPHOD);
    changeset.setDate(creationTime);

    when(serviceFactory.create(any(NamespaceAndName.class))).thenReturn(repositoryService);
    when(repositoryService.getLogCommand()).thenReturn(logCommandBuilder);
    when(logCommandBuilder.getChangesets()).thenReturn(new ChangesetPagingResult(1, ImmutableList.of(changeset)));
    Branch branch = Branch.normalBranch("master", "42");

    BranchDto dto = mapper.map(branch, new NamespaceAndName("hitchhiker", "heart-of-gold"));

    assertThat(dto.getLastModified()).isEqualTo(Instant.ofEpochMilli(creationTime));
    assertThat(dto.getLastModifier().getName()).isEqualTo(PersonTestData.ZAPHOD.getName());
  }

}
