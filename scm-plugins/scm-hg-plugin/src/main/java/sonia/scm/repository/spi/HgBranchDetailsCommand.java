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

import org.javahg.Changeset;
import org.javahg.Repository;
import sonia.scm.repository.api.BranchDetailsCommandResult;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.javahg.commands.flags.LogCommandFlags.on;

public class HgBranchDetailsCommand implements BranchDetailsCommand {

  private static final String DEFAULT_BRANCH_NAME = "default";
  private static final int CHANGESET_LIMIT = 9999999;

  private final HgCommandContext context;

  @Inject
  HgBranchDetailsCommand(HgCommandContext context) {
    this.context = context;
  }

  @Override
  public BranchDetailsCommandResult execute(BranchDetailsCommandRequest request) {
    Repository repository = context.open();

    if (request.getBranchName().equals(DEFAULT_BRANCH_NAME)) {
      return new BranchDetailsCommandResult(0,0);
    }

    // To get the latest shared ancestor
    // hg log --rev "ancestor('2.0.0-m3','feature/ui_deviation')"

    List<Changeset> latestAncestor = on(repository).rev(String.format("'%s' + '%s' and merge()", DEFAULT_BRANCH_NAME, request.getBranchName())).execute();
    List<Changeset> headChangesets = on(repository).limit(CHANGESET_LIMIT).branch(DEFAULT_BRANCH_NAME).execute();
    List<Changeset> branchChangesets = on(repository).limit(CHANGESET_LIMIT).branch(request.getBranchName()).execute();

    List<Changeset> behind = on(repository).rev(
      String.format(
        "%s:%s", 
        latestAncestor.get(0).getRevision(), 
        branchChangesets.get(0).getRevision()
      )
    ).execute();
    behind.removeAll(branchChangesets);

    List<Changeset> ahead = new ArrayList<>(branchChangesets);
    ahead.removeAll(headChangesets);

    return new BranchDetailsCommandResult(ahead.size(), behind.size());
  }

}
