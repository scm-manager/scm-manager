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

package sonia.scm.repository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.ISVNAnnotateHandler;

import sonia.scm.util.Util;

import java.io.File;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("deprecation")
public class SvnBlameHandler implements ISVNAnnotateHandler
{
  private static final Logger logger =
    LoggerFactory.getLogger(SvnBlameHandler.class);

  private final List<BlameLine> blameLines;

  private final Map<Long, String> descriptionCache = new HashMap<>();

  private final String path;

  private final SVNRepository svnRepository;

  public SvnBlameHandler(SVNRepository svnRepository, String path,
                         List<BlameLine> blameLines)
  {
    this.svnRepository = svnRepository;
    this.path = path;
    this.blameLines = blameLines;
  }


   @Override
  public void handleEOF()
  {

    // do nothing
  }

  @Override
  public void handleLine(Date date, long revision, String author, String line)
          throws SVNException
  {
    handleLine(date, revision, author, line, null, -1, null, null, 0);
  }

  @Override
  public void handleLine(Date date, long revision, String author, String line,
                         Date mergedDate, long mergedRevision,
                         String mergedAuthor, String mergedPath, int lineNumber)
          throws SVNException
  {
    Person authorPerson = null;

    if (Util.isNotEmpty(author))
    {
      authorPerson = Person.toPerson(author);
    }

    Long when = null;

    if (date != null)
    {
      when = date.getTime();
    }

    String description = getDescription(revision);

    blameLines.add(new BlameLine(lineNumber + 1, String.valueOf(revision),
                                 when, authorPerson, description, line));
  }

  @Override
  public boolean handleRevision(Date date, long revision, String author,
                                File contents)
          throws SVNException
  {
    return false;
  }



  @SuppressWarnings("unchecked")
  private String getDescription(long revision)
  {
    String description = descriptionCache.get(revision);

    if (description == null)
    {
      try
      {
        Collection<SVNLogEntry> entries = svnRepository.log(new String[] {
                                            path }, null, revision, revision,
                                              true, true);

        for (SVNLogEntry entry : entries)
        {
          if (revision == entry.getRevision())
          {
            description = entry.getMessage();
            descriptionCache.put(revision, description);

            break;
          }
        }
      }
      catch (SVNException ex)
      {
        logger.warn(
            "could not retrive description for revision ".concat(
              String.valueOf(revision)), ex);
      }
    }

    return description;
  }

}
