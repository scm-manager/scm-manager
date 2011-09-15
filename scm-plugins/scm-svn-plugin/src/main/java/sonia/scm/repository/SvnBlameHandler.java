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

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNAnnotateHandler;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnBlameHandler implements ISVNAnnotateHandler
{

  /**
   * Constructs ...
   *
   *
   * @param blameLines
   */
  public SvnBlameHandler(List<BlameLine> blameLines)
  {
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
      authorPerson = new Person(author);
    }

    blameLines.add(new BlameLine(authorPerson, date, String.valueOf(revision),
                                 line, lineNumber));
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private List<BlameLine> blameLines;
}
