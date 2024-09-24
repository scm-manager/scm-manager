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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class SvnBlameCommandTest extends AbstractSvnCommandTestBase
{

  @Test
  public void testGetBlameResult()
  {
    BlameCommandRequest request = new BlameCommandRequest();

    request.setPath("a.txt");

    BlameResult result = createCommand().getBlameResult(request);

    assertNotNull(result);
    assertEquals(2, result.getTotal());

    BlameLine line = result.getLine(0);

    checkFirstLine(line);
    line = result.getLine(1);
    assertEquals(2, line.getLineNumber());
    assertEquals("5", line.getRevision());
    checkDate(line.getWhen());
    assertEquals("line for blame test", line.getCode());
    assertEquals("added line for blame test", line.getDescription());
    assertEquals("zaphod", line.getAuthor().getName());
    assertNull(line.getAuthor().getMail());
  }

  @Test
  public void testGetBlameResultWithRevision() {
    BlameCommandRequest request = new BlameCommandRequest();

    request.setPath("a.txt");
    request.setRevision("3");

    BlameResult result = createCommand().getBlameResult(request);

    assertNotNull(result);
    assertEquals(1, result.getTotal());

    BlameLine line = result.getLine(0);

    checkFirstLine(line);
  }


  private void checkFirstLine(BlameLine line)
  {
    assertEquals(1, line.getLineNumber());
    assertEquals("3", line.getRevision());
    checkDate(line.getWhen());
    assertEquals("a and b", line.getCode());
    assertEquals("remove b and modified a", line.getDescription());
    assertEquals("perfect", line.getAuthor().getName());
    assertNull(line.getAuthor().getMail());
  }

  
  private SvnBlameCommand createCommand()
  {
    return new SvnBlameCommand(createContext());
  }
}
