/**
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

package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.ISVNAnnotateHandler;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
@SuppressWarnings("deprecation")
public class SvnBlameHandler implements ISVNAnnotateHandler
{

  /** the logger for SvnBlameHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnBlameHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param svnRepository
   * @param path
   * @param blameLines
   */
  public SvnBlameHandler(SVNRepository svnRepository, String path,
                         List<BlameLine> blameLines)
  {
    this.svnRepository = svnRepository;
    this.path = path;
    this.blameLines = blameLines;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void handleEOF()
  {

    // do nothing
  }

  /**
   * Method description
   *
   *
   * @param date
   * @param revision
   * @param author
   * @param line
   *
   * @throws SVNException
   */
  @Override
  public void handleLine(Date date, long revision, String author, String line)
          throws SVNException
  {
    handleLine(date, revision, author, line, null, -1, null, null, 0);
  }

  /**
   * Method description
   *
   *
   * @param date
   * @param revision
   * @param author
   * @param line
   * @param mergedDate
   * @param mergedRevision
   * @param mergedAuthor
   * @param mergedPath
   * @param lineNumber
   *
   * @throws SVNException
   */
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

  /**
   * Method description
   *
   *
   * @param date
   * @param revision
   * @param author
   * @param contents
   *
   * @return
   *
   * @throws SVNException
   */
  @Override
  public boolean handleRevision(Date date, long revision, String author,
                                File contents)
          throws SVNException
  {
    return false;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   *
   * @return
   */
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final List<BlameLine> blameLines;

  /** Field description */
  private final Map<Long, String> descriptionCache = new HashMap<>();

  /** Field description */
  private final String path;

  /** Field description */
  private final SVNRepository svnRepository;
}
