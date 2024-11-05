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
import org.javahg.Branch;
import org.javahg.Changeset;
import org.javahg.commands.BranchesCommand;
import org.javahg.commands.LogCommand;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.spi.javahg.HgFileviewCommand;

import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

/**
 * Utilizes the mercurial fileview extension in order to support mercurial repository browsing.
 *
 */
public class HgBrowseCommand extends AbstractCommand implements BrowseCommand
{

  @Inject
  public HgBrowseCommand(@Assisted HgCommandContext context)
  {
    super(context);
  }


  @Override
  public BrowserResult getBrowserResult(BrowseCommandRequest request) throws IOException {
    HgFileviewCommand cmd = HgFileviewCommand.on(open());

    String revision = MoreObjects.firstNonNull(request.getRevision(), "tip");
    Changeset c = LogCommand.on(getContext().open()).rev(revision).limit(1).single();

    if (c != null) {
      cmd.rev(c.getNode());
    }

    if (!Strings.isNullOrEmpty(request.getPath()))
    {
      cmd.path(request.getPath());
    }

    if (request.isDisableLastCommit())
    {
      cmd.disableLastCommit();
    }

    if (request.isRecursive())
    {
      cmd.recursive();
    }

    if (request.isDisableSubRepositoryDetection())
    {
      cmd.disableSubRepositoryDetection();
    }

    cmd.setLimit(request.getLimit());
    cmd.setOffset(request.getOffset());

    FileObject file = cmd.execute()
      .orElseThrow(() -> notFound(entity("File", request.getPath()).in("Revision", revision).in(getRepository())));
    boolean requestedRevisionIsBranch = BranchesCommand
      .on(getContext().open())
      .execute()
      .stream()
      .map(Branch::getName)
      .anyMatch(b -> b.equals(request.getRevision()));
    return new BrowserResult(c == null ? "tip" : c.getNode(), revision, file, requestedRevisionIsBranch);
  }

  public interface Factory {
    HgBrowseCommand create(HgCommandContext context);
  }

}
