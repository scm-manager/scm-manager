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

package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.assistedinject.Assisted;
import org.javahg.Changeset;
import org.javahg.commands.AnnotateCommand;
import org.javahg.AnnotateLine;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.BlameLine;
import sonia.scm.repository.BlameResult;
import sonia.scm.repository.Person;
import sonia.scm.web.HgUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

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
   *  @param context
   *
   */

  @Inject
  HgBlameCommand(@Assisted HgCommandContext context)
  {
    super(context);
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  public BlameResult getBlameResult(BlameCommandRequest request)
    throws IOException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("get blame result for {}", request);
    }

    AnnotateCommand cmd = AnnotateCommand.on(open());

    cmd.rev(HgUtil.getRevision(request.getRevision()));

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

  public interface Factory {
    HgBlameCommand create(HgCommandContext context);
  }

}
