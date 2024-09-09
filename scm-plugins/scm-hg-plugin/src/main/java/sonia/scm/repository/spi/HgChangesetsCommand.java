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
import sonia.scm.repository.Changeset;
import sonia.scm.repository.spi.javahg.HgLogChangesetCommand;

import java.util.Optional;

import static sonia.scm.repository.spi.javahg.HgLogChangesetCommand.on;

public class HgChangesetsCommand extends AbstractCommand implements ChangesetsCommand {

  @Inject
  public HgChangesetsCommand(@Assisted HgCommandContext context) {
    super(context);
  }

  @Override
  public Iterable<Changeset> getChangesets(ChangesetsCommandRequest request) {
    org.javahg.Repository repository = open();
    HgLogChangesetCommand cmd = on(repository, context.getConfig());
    // Get all changesets between the first changeset and the repository tip, both inclusive.
    cmd.rev("tip:0");
    return cmd.execute();
  }

  @Override
  public Optional<Changeset> getLatestChangeset() {
    org.javahg.Repository repository = open();
    HgLogChangesetCommand cmd = on(repository, context.getConfig());
    Changeset tip = cmd.rev("tip").single();
    if (tip != null) {
      return Optional.of(tip);
    }
    return Optional.empty();
  }

  public interface Factory {
    HgChangesetsCommand create(HgCommandContext context);
  }

}
