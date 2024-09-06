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

import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.BranchDetails;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class BranchDetailsMapperTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();

  BranchDetailsMapper mapper = new BranchDetailsMapperImpl();

  @BeforeEach
  void configureMapper() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("/scm/api/"));
    mapper.setResourceLinks(new ResourceLinks(Providers.of(scmPathInfoStore)));
  }

  @Test
  void shouldMapDto() {
    BranchDetailsDto dto = mapper.map(
      repository,
      "master",
      new BranchDetails("master", 42, 21)
    );

    assertThat(dto.getBranchName()).isEqualTo("master");
    assertThat(dto.getChangesetsAhead()).isEqualTo(42);
    assertThat(dto.getChangesetsBehind()).isEqualTo(21);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref())
      .isEqualTo("/scm/api/v2/repositories/hitchhiker/42Puzzle/branch-details/master");
  }
}
