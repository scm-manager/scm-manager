/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgRepositoryHookEvent extends AbstractRepositoryHookEvent
{

  /** Field description */
  public static final String REV_TIP = "tip";

  /** the logger for HgRepositoryHookEvent */
  private static final Logger logger =
    LoggerFactory.getLogger(HgRepositoryHookEvent.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repositoryName
   * @param startRev
   * @param type
   */
  public HgRepositoryHookEvent(HgRepositoryHandler handler,
                               String repositoryName, String startRev,
                               RepositoryHookType type)
  {
    this.handler = handler;
    this.repositoryName = repositoryName;
    this.startRev = startRev;
    this.type = type;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Changeset> getChangesets()
  {
    if (changesets == null)
    {
      try
      {
        changesets = createChangesetViewer().getChangesets(startRev, REV_TIP,
                true);
      }
      catch (IOException ex)
      {
        logger.error("could not load changesets", ex);
      }
    }

    return changesets;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public RepositoryHookType getType()
  {
    return type;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private HgChangesetViewer createChangesetViewer()
  {
    File directory = handler.getConfig().getRepositoryDirectory();
    File repositoryDirectory = new File(directory, repositoryName);

    return new HgChangesetViewer(handler,
                                 repositoryDirectory.getAbsolutePath());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private List<Changeset> changesets;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private String repositoryName;

  /** Field description */
  private String startRev;

  /** Field description */
  private RepositoryHookType type;
}
