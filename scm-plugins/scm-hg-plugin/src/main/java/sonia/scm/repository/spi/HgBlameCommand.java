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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.Changeset;
import com.aragost.javahg.commands.AnnotateCommand;
import com.aragost.javahg.commands.AnnotateLine;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.BlameLine;
import sonia.scm.repository.BlameResult;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgBlameCommand extends AbstractCommand implements BlameCommand
{

  /**
   * the logger for HgBlameCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HgBlameCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param repository
   */
  public HgBlameCommand(HgCommandContext context, Repository repository)
  {
    super(context, repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public BlameResult getBlameResult(BlameCommandRequest request)
          throws IOException, RepositoryException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("get blame result for {}", request);
    }

    AnnotateCommand cmd = AnnotateCommand.on(open());

    if (!Strings.isNullOrEmpty(request.getRevision()))
    {
      cmd.rev(request.getRevision());
    }

    List<BlameLine> blameLines = Lists.newArrayList();
    List<AnnotateLine> lines = cmd.execute(request.getPath());
    int counter = 0;

    for (AnnotateLine line : lines)
    {
      blameLines.add(convert(line, ++counter));
    }

    return new BlameResult(blameLines);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param line
   * @param counter
   *
   * @return
   */
  private BlameLine convert(AnnotateLine line, int counter)
  {
    BlameLine blameLine = new BlameLine();

    blameLine.setLineNumber(counter);
    blameLine.setCode(line.getLine());

    Changeset c = line.getChangeset();

    blameLine.setDescription(c.getMessage());
    blameLine.setWhen(c.getTimestamp().getDate().getTime());
    blameLine.setRevision(c.getNode());

    String user = c.getUser();

    if (!Strings.isNullOrEmpty(user))
    {
      blameLine.setAuthor(Person.toPerson(user));
    }

    return blameLine;
  }
}
