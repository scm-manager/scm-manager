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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultBranchLinkProviderTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ResourceLinks resourceLinks;

  @InjectMocks
  private DefaultBranchLinkProvider branchLinkProvider;

  @Test
  void shouldReturnBranchLink() {
    when(resourceLinks.branch()).thenReturn(new ResourceLinks.BranchLinks(() -> URI.create("/")));
    Repository repository = RepositoryTestData.createHeartOfGold();
    String branch = "develop";

    String branchLink = branchLinkProvider.get(repository.getNamespaceAndName(), branch);

    assertThat(branchLink).isEqualTo("/v2/repositories/hitchhiker/HeartOfGold/branches/develop");
  }

}
