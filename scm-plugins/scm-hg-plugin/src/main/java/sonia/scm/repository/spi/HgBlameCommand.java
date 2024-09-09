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

package sonia.scm.repository.spi;


import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
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

import java.io.IOException;
import java.util.List;


public class HgBlameCommand extends AbstractCommand implements BlameCommand
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(HgBlameCommand.class);



  @Inject
  HgBlameCommand(@Assisted HgCommandContext context)
  {
    super(context);
  }


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
