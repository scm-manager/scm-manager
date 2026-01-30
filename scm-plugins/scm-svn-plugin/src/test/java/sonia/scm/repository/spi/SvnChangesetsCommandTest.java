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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Changeset;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SvnChangesetsCommandTest {

  private SvnChangesetsCommand createCommand(SvnContext context) {
    return new SvnChangesetsCommand(context);
  }

  @Nested
  class GetChangesets extends AbstractSvnCommandTestBase {
    @Test
    void getAllChangesetsFromRepository() {
      Iterable<Changeset> changesets = createCommand(createContext()).getChangesets(new ChangesetsCommandRequest());

      assertThat(changesets).hasSize(5);
    }
  }

  @Nested
  class GetLatestChangesets extends AbstractSvnCommandTestBase {

    @Test
    void getLatestChangesetFromRepository() {
      Optional<Changeset> changeset = createCommand(createContext()).getLatestChangeset();

      assertThat(changeset).isPresent();
      assertThat(changeset.get().getId()).isEqualTo("5");
    }
  }

  @Nested
  class GetChangesets_Empty extends AbstractSvnCommandTestBase {
    @Test
    void shouldReturnEmptyIterableWithEmptyRepository() {
      Iterable<Changeset> changesets = createCommand(createContext()).getChangesets(new ChangesetsCommandRequest());

      assertThat(changesets).isEmpty();
    }


    @Override
    protected String getZippedRepositoryResource() {
      return "sonia/scm/repository/spi/scm-svn-spi-empty-test.zip";
    }
  }

}
