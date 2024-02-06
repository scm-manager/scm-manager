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
