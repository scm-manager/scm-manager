/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.spi.HgHookChangesetProvider;
import sonia.scm.repository.spi.HookChangesetRequest;
import sonia.scm.repository.spi.javahg.AbstractChangesetCommand;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgHookBranchProvider implements HookBranchProvider
{

  /** Field description */
  private static final HookChangesetRequest REQUEST =
    new HookChangesetRequest();

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param changesetProvider
   */
  public HgHookBranchProvider(HgHookChangesetProvider changesetProvider)
  {
    this.changesetProvider = changesetProvider;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<String> getCreatedOrModified()
  {
    if (createdOrModified == null)
    {
      collect();
    }

    return createdOrModified;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<String> getDeletedOrClosed()
  {
    if (deletedOrClosed == null)
    {
      collect();
    }

    return deletedOrClosed;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param builder
   * @param c
   *
   * @return
   */
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

  /**
   * Method description
   *
   *
   * @return
   */
  private Iterable<Changeset> changesets()
  {
    return changesetProvider.handleRequest(REQUEST).getChangesets();
  }

  /**
   * Method description
   *
   */
  private void collect()
  {
    Builder<String> createdOrModifiedBuilder = ImmutableList.builder();
    Builder<String> deletedOrClosedBuilder = ImmutableList.builder();

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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final HgHookChangesetProvider changesetProvider;

  /** Field description */
  private List<String> createdOrModified;

  /** Field description */
  private List<String> deletedOrClosed;
}
