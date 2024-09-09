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

package sonia.scm.repository.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.spi.HookChangesetRequest;
import sonia.scm.repository.spi.javahg.AbstractChangesetCommand;
import sonia.scm.util.Util;

import java.util.List;

/**
 * Mercurial hook branch provider implementation.
 *
 */
public class HgHookBranchProvider implements HookBranchProvider
{

  private static final Logger logger = LoggerFactory.getLogger(HgHookBranchProvider.class);

  private static final HookChangesetRequest REQUEST =
    new HookChangesetRequest();

  private final HookChangesetProvider changesetProvider;

  private List<String> createdOrModified;

  private List<String> deletedOrClosed;

  public HgHookBranchProvider(HookChangesetProvider changesetProvider)
  {
    this.changesetProvider = changesetProvider;
  }


  @Override
  public List<String> getCreatedOrModified()
  {
    if (createdOrModified == null)
    {
      collect();
    }

    return createdOrModified;
  }

  @Override
  public List<String> getDeletedOrClosed()
  {
    if (deletedOrClosed == null)
    {
      collect();
    }

    return deletedOrClosed;
  }


  private List<String> appendBranches(Builder<String> builder, Changeset c)
  {
    List<String> branches = c.getBranches();

    if (Util.isEmpty(branches))
    {
      builder.add(AbstractChangesetCommand.BRANCH_DEFAULT);
    }
    else
    {
      builder.addAll(branches);
    }

    return branches;
  }

  private Iterable<Changeset> changesets()
  {
    return changesetProvider.handleRequest(REQUEST).getChangesets();
  }

  private void collect()
  {
    Builder<String> createdOrModifiedBuilder = ImmutableList.builder();
    Builder<String> deletedOrClosedBuilder = ImmutableList.builder();

    logger.trace("collecting branches from hook changesets");

    for (Changeset c : changesets())
    {
      if (c.getProperty(AbstractChangesetCommand.PROPERTY_CLOSE) != null)
      {
        appendBranches(deletedOrClosedBuilder, c);
      }
      else
      {
        appendBranches(createdOrModifiedBuilder, c);
      }
    }

    createdOrModified = createdOrModifiedBuilder.build();
    deletedOrClosed = deletedOrClosedBuilder.build();
  }

}
