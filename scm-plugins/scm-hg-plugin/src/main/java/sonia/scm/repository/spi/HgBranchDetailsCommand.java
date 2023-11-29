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
