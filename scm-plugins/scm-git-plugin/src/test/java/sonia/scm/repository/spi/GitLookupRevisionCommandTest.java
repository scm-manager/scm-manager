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

package sonia.scm.repository.spi;

import org.junit.jupiter.api.Test;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.api.LookupRevisionCommandResult;

import static org.assertj.core.api.Assertions.assertThat;

class GitLookupRevisionCommandTest extends AbstractGitCommandTestBase {

  @Test
  void shouldReturnEmptyListIfNoRevisionMatches() {
    GitLookupRevisionCommand command = createCommand();

    LookupRevisionCommandRequest request = createRequest("abc");
    LookupRevisionCommandResult result = command.getChangesets(request);

    assertThat(result.getChangesets()).isEmpty();
  }

  @Test
  void shouldReturnExistingCommitsOnly() {
    GitLookupRevisionCommand command = createCommand();

    LookupRevisionCommandRequest request = createRequest("a8");
    LookupRevisionCommandResult result = command.getChangesets(request);

    assertThat(result.getChangesets())
      .extracting("id")
      .containsExactly("a8495c0335a13e6e432df90b3727fa91943189a7");
  }

  private static LookupRevisionCommandRequest createRequest(String prefix) {
    LookupRevisionCommandRequest request = new LookupRevisionCommandRequest();
    request.setRevisionPrefix(prefix);
    return request;
  }

  private GitLookupRevisionCommand createCommand() {
    return new GitLookupRevisionCommand(createContext(), GitTestHelper.createConverterFactory());
  }
}
