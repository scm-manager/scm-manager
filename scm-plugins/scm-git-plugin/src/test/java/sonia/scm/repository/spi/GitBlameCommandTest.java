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
import sonia.scm.repository.GitRepositoryConfig;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link GitBlameCommand}.
 *
 */
public class GitBlameCommandTest extends AbstractGitCommandTestBase
{

  /**
   * Tests blame command with default branch.
   *
   * @throws IOException
   * @
   */
  @Test
  public void testDefaultBranch() throws IOException {
    // without default branch, the repository head should be used
    BlameCommandRequest request = new BlameCommandRequest();
    request.setPath("a.txt");

    BlameResult result = createCommand().getBlameResult(request);
    assertNotNull(result);
    assertEquals(2, result.getTotal());
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca", result.getLine(0).getRevision());
    assertEquals("fcd0ef1831e4002ac43ea539f4094334c79ea9ec", result.getLine(1).getRevision());

    // set default branch and test again
    createContext().setConfig(new GitRepositoryConfig("test-branch"));
    result = createCommand().getBlameResult(request);
    assertNotNull(result);
    assertEquals(1, result.getTotal());
    assertEquals("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4", result.getLine(0).getRevision());
  }


  @Test
  public void testGetBlameResult() throws IOException
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
    assertEquals("fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
                 line.getRevision());
    checkDate(line.getWhen());
    assertEquals("line for blame", line.getCode());
    assertEquals("added new line for blame", line.getDescription());
    assertEquals("Zaphod Beeblebrox", line.getAuthor().getName());
    assertEquals("zaphod.beeblebrox@hitchhiker.com",
                 line.getAuthor().getMail());
  }


  @Test
  public void testGetBlameResultWithRevision()
          throws IOException
  {
    BlameCommandRequest request = new BlameCommandRequest();

    request.setPath("a.txt");
    request.setRevision("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");

    BlameResult result = createCommand().getBlameResult(request);

    assertNotNull(result);
    assertEquals(1, result.getTotal());

    BlameLine line = result.getLine(0);

    checkFirstLine(line);
  }


  private void checkFirstLine(BlameLine line)
  {
    assertEquals(1, line.getLineNumber());
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca",
                 line.getRevision());
    checkDate(line.getWhen());
    assertEquals("a", line.getCode());
    assertEquals("added a and b files", line.getDescription());
    assertEquals("Douglas Adams", line.getAuthor().getName());
    assertEquals("douglas.adams@hitchhiker.com", line.getAuthor().getMail());
  }


  private GitBlameCommand createCommand()
  {
    return new GitBlameCommand(createContext());
  }
}
