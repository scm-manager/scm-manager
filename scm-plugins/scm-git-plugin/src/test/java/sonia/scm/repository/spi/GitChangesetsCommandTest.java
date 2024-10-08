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

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitTestHelper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class GitChangesetsCommandTest extends AbstractGitCommandTestBase {

  @Test
  public void shouldFindLatestCommit() {
    GitChangesetsCommand command = new GitChangesetsCommand(createContext(), GitTestHelper.createConverterFactory());
    Optional<Changeset> changeset = command.getLatestChangeset();
    assertThat(changeset).get().extracting("id").isEqualTo("a8495c0335a13e6e432df90b3727fa91943189a7");
  }

  @Test
  public void shouldListAllRevisions() {
    GitChangesetsCommand command = new GitChangesetsCommand(createContext(), GitTestHelper.createConverterFactory());
    Iterable<Changeset> changesets = command.getChangesets(new ChangesetsCommandRequest());
    assertThat(changesets)
      .extracting("id")
      .hasSize(19)
      .contains(
        "a8495c0335a13e6e432df90b3727fa91943189a7",
        "03ca33468c2094249973d0ca11b80243a20de368",
        "9e93d8631675a89615fac56b09209686146ff3c0",
        "383b954b27e052db6880d57f1c860dc208795247",
        "1fcebf45a215a43f0713a57b807d55e8387a6d70",
        "9f28cf5eb3a4df05d284c6f2d276c20c0f0e5b6c",
        "674ca9a2208df60224b0f33beeea5259b374d2d0",
        "35597e9e98fe53167266583848bfef985c2adb27",
        "f360a8738e4a29333786c5817f97a2c912814536",
        "d1dfecbfd5b4a2f77fe40e1bde29e640f7f944be",
        "d81ad6c63d7e2162308d69637b339dedd1d9201c",
        "6a2abf9dca5cff5d76720d1276e1112d9c75ea60",
        "2f95f02d9c568594d31e78464bd11a96c62e3f91",
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
        "86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1",
        "91b99de908fcd04772798a31c308a64aea1a5523",
        "3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4",
        "592d797cd36432e591416e8b2b98154f4f163411",
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      );
  }
}
