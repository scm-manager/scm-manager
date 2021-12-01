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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.BranchDetailsCommandResult;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class BranchDetailsMapperTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();

  BranchDetailsMapper mapper = new BranchDetailsMapperImpl();

  @BeforeEach
  void configureMapper() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("/scm/api/"));
    mapper.setResourceLinks(new ResourceLinks(scmPathInfoStore));
  }

  @Test
  void shouldMapDto() {
    BranchDetailsDto dto = mapper.map(
      repository,
      "master",
      new BranchDetailsCommandResult(42, 21)
    );

    assertThat(dto.getBranchName()).isEqualTo("master");
    assertThat(dto.getChangesetsAhead()).isEqualTo(42);
    assertThat(dto.getChangesetsBehind()).isEqualTo(21);
    assertThat(dto.getLinks().getLinkBy("self").get().getHref())
      .isEqualTo("/scm/api/v2/repositories/hitchhiker/42Puzzle/branch-details/master");
  }
}
