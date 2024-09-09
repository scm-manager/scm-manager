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

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import sonia.scm.repository.Modification;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.spi.javahg.HgLogChangesetCommand;
import sonia.scm.repository.spi.javahg.StateCommand;

import java.io.IOException;
import java.util.Collection;

public class HgModificationsCommand extends AbstractCommand implements ModificationsCommand {

  @Inject
  HgModificationsCommand(@Assisted HgCommandContext context) {
    super(context);
  }

  @Override
  public Modifications getModifications(String revision) {
    org.javahg.Repository repository = open();
    HgLogChangesetCommand hgLogChangesetCommand = HgLogChangesetCommand.on(repository, getContext().getConfig());
    Collection<Modification> modifications = hgLogChangesetCommand.rev(revision).extractModifications();
    return new Modifications(revision, modifications);
  }

  @Override
  public Modifications getModifications(String baseRevision, String revision) throws IOException {
    org.javahg.Repository repository = open();
    StateCommand stateCommand = new StateCommand(repository);
    return new Modifications(baseRevision, revision, stateCommand.call(baseRevision, revision));
  }

  public interface Factory {
    HgModificationsCommand create(HgCommandContext context);
  }

}
