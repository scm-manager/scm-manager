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
