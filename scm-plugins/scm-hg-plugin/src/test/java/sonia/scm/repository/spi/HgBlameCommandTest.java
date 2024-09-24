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


import org.junit.Test;
import sonia.scm.repository.BlameLine;
import sonia.scm.repository.BlameResult;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class HgBlameCommandTest extends AbstractHgCommandTestBase
{

  @Test
  public void testGetBlameResult() throws IOException {
    BlameCommandRequest request = new BlameCommandRequest();

    request.setPath("a.txt");

    BlameResult result = createCommand().getBlameResult(request);

    assertNotNull(result);
    assertEquals(2, result.getTotal());

    BlameLine line = result.getLine(0);

    checkFirstLine(line);
    line = result.getLine(1);
    assertEquals(2, line.getLineNumber());
    assertEquals("2baab8e80280ef05a9aa76c49c76feca2872afb7", line.getRevision());
    checkDate(line.getWhen());
    assertEquals("line for blame", line.getCode());
    assertEquals("added new line for blame", line.getDescription());
    assertEquals("Zaphod Beeblebrox", line.getAuthor().getName());
    assertEquals("zaphod.beeblebrox@hitchhiker.com",
                 line.getAuthor().getMail());
  }

  @Test
  public void testGetBlameResultWithRevision() throws IOException {
    BlameCommandRequest request = new BlameCommandRequest();

    request.setPath("a.txt");
    request.setRevision("a9bacaf1b7fa0cebfca71fed4e59ed69a6319427");

    BlameResult result = createCommand().getBlameResult(request);

    assertNotNull(result);
    assertEquals(1, result.getTotal());

    BlameLine line = result.getLine(0);

    checkFirstLine(line);
  }


  private void checkFirstLine(BlameLine line)
  {
    assertEquals(1, line.getLineNumber());
    assertEquals("a9bacaf1b7fa0cebfca71fed4e59ed69a6319427", line.getRevision());
    checkDate(line.getWhen());
    assertEquals("a", line.getCode());
    assertEquals("added a and b files", line.getDescription());
    assertEquals("Douglas Adams", line.getAuthor().getName());
    assertEquals("douglas.adams@hitchhiker.com", line.getAuthor().getMail());
  }


  private BlameCommand createCommand()
  {
    return new HgBlameCommand(cmdContext);
  }
}
