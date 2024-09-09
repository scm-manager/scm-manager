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
import sonia.scm.repository.Changeset;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SvnChangesetsCommandTest extends AbstractSvnCommandTestBase {

  @Test
  public void getAllChangesetsFromRepository() {
    Iterable<Changeset> changesets = createCommand().getChangesets(new ChangesetsCommandRequest());

    assertThat(changesets).hasSize(5);
  }

  @Test
  public void getLatestChangesetFromRepository() {
    Optional<Changeset> changeset = createCommand().getLatestChangeset();

    assertThat(changeset).isPresent();
    assertThat(changeset.get().getId()).isEqualTo("5");
  }

  private SvnChangesetsCommand createCommand()
  {
    return new SvnChangesetsCommand(createContext());
  }

}
