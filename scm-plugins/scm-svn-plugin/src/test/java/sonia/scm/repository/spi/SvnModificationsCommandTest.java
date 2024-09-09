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
import sonia.scm.repository.Modifications;

import static org.assertj.core.api.Assertions.assertThat;

public class SvnModificationsCommandTest extends AbstractSvnCommandTestBase {

  @Test
  public void shouldReadModificationsForSingleRevision() {
    SvnContext context = createContext();
    SvnModificationsCommand svnModificationsCommand = new SvnModificationsCommand(context);

    Modifications modifications = svnModificationsCommand.getModifications("4");

    assertThat(modifications.getAdded()).hasSize(3);
  }

  @Test
  public void shouldReadModificationsForMultipleRevisions() {
    SvnContext context = createContext();
    SvnModificationsCommand svnModificationsCommand = new SvnModificationsCommand(context);

    Modifications modifications = svnModificationsCommand.getModifications("1", "4");

    assertThat(modifications.getModifications()).hasSize(4);
  }
}
