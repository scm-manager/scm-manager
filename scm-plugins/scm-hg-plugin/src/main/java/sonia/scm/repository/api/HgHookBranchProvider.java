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
