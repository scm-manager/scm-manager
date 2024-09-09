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
import org.javahg.Changeset;
import org.javahg.commands.ExecutionException;
import org.javahg.commands.LogCommand;
import sonia.scm.repository.Branch;
import sonia.scm.repository.BranchDetails;
import sonia.scm.repository.api.BranchDetailsCommandResult;

import java.util.List;

import static org.javahg.commands.flags.LogCommandFlags.on;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;
import static sonia.scm.repository.spi.javahg.AbstractChangesetCommand.CHANGESET_LAZY_STYLE_PATH;

public class HgBranchDetailsCommand implements BranchDetailsCommand {

  private static final String DEFAULT_BRANCH_NAME = "default";

  private final HgCommandContext context;

  @Inject
  HgBranchDetailsCommand(@Assisted HgCommandContext context) {
    this.context = context;
  }

  @Override
  public BranchDetailsCommandResult execute(BranchDetailsCommandRequest request) {
    final String branchName = request.getBranchName();
    if (branchName.equals(DEFAULT_BRANCH_NAME)) {
      return new BranchDetailsCommandResult(new BranchDetails(branchName, 0, 0));
    }

    try {
      List<Changeset> behind = getChangesetsSolelyOnBranch(DEFAULT_BRANCH_NAME, branchName);
      List<Changeset> ahead = getChangesetsSolelyOnBranch(branchName, DEFAULT_BRANCH_NAME);

      return new BranchDetailsCommandResult(new BranchDetails(branchName, ahead.size(), behind.size()));
    } catch (ExecutionException e) {
      if (e.getMessage().contains("unknown revision '")) {
        throw notFound(entity(Branch.class, branchName).in(context.getScmRepository()));
      }
      throw e;
    }
  }

  private List<Changeset> getChangesetsSolelyOnBranch(String branch, String reference) {
    LogCommand logCommand = on(context.open()).rev(
      String.format(
        "'%s' %% '%s'",
        branch,
        reference
      )
    );
    logCommand.cmdAppend("--style", CHANGESET_LAZY_STYLE_PATH);
    return logCommand.execute();
  }

  public interface Factory {
    HgBranchDetailsCommand create(HgCommandContext context);
  }
}
