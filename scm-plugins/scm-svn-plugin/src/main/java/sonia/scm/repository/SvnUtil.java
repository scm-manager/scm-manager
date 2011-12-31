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

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.wc.admin.SVNChangeEntry;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnUtil
{

  /**
   * TODO: type replaced
   *
   *
   * @param modifications
   * @param entry
   */
  public static void appendModification(Modifications modifications,
          SVNLogEntryPath entry)
  {
    appendModification(modifications, entry.getType(), entry.getPath());
  }

  /**
   * Method description
   *
   *
   * @param modifications
   * @param entry
   */
  public static void appendModification(Modifications modifications,
          SVNChangeEntry entry)
  {
    appendModification(modifications, entry.getType(), entry.getPath());
  }

  /**
   * Method description
   *
   *
   * @param modifications
   * @param type
   * @param path
   */
  public static void appendModification(Modifications modifications, char type,
          String path)
  {
    switch (type)
    {
      case SVNLogEntryPath.TYPE_ADDED :
        modifications.getAdded().add(path);

        break;

      case SVNLogEntryPath.TYPE_DELETED :
        modifications.getRemoved().add(path);

        break;

      case SVNLogEntryPath.TYPE_MODIFIED :
        modifications.getModified().add(path);

        break;
    }
  }

  /**
   * Method description
   *
   *
   * @param entry
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public static Changeset createChangeset(SVNLogEntry entry)
  {
    long revision = entry.getRevision();
    Changeset changeset = new Changeset(String.valueOf(revision),
                            entry.getDate().getTime(),
                            Person.toPerson(entry.getAuthor()),
                            entry.getMessage());

    if (revision > 0)
    {
      changeset.getParents().add(String.valueOf(revision - 1));
    }

    Map<String, SVNLogEntryPath> changeMap = entry.getChangedPaths();

    if (Util.isNotEmpty(changeMap))
    {
      Modifications modifications = changeset.getModifications();

      for (SVNLogEntryPath e : changeMap.values())
      {
        appendModification(modifications, e);
      }
    }

    return changeset;
  }
}
