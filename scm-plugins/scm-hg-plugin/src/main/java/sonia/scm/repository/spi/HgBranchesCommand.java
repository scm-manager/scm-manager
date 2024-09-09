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
import com.google.common.collect.Lists;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Person;

import java.util.List;


public class HgBranchesCommand extends AbstractCommand
  implements BranchesCommand
{

  private static final String DEFAULT_BRANCH_NAME = "default";

  @Inject
  public HgBranchesCommand(@Assisted HgCommandContext context)
  {
    super(context);
  }


  @Override
  public List<Branch> getBranches() {
    List<org.javahg.Branch> hgBranches =
      org.javahg.commands.BranchesCommand.on(open()).execute();

    return Lists.transform(hgBranches,
      hgBranch -> {
        String node = null;
        Changeset changeset = hgBranch.getBranchTip();

        if (changeset != null)
        {
          node = changeset.getNode();
        }

        Person lastCommitter = Person.toPerson(changeset.getUser());
        long lastCommitDate = changeset.getTimestamp().getDate().getTime();
        if (DEFAULT_BRANCH_NAME.equals(hgBranch.getName())) {
          return Branch.defaultBranch(hgBranch.getName(), node, lastCommitDate, lastCommitter);
        } else {
          return Branch.normalBranch(hgBranch.getName(), node, lastCommitDate, lastCommitter);
        }
      });
  }

  public interface Factory {
    HgBranchesCommand create(HgCommandContext context);
  }

}
