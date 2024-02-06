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


import org.javahg.Changeset;
import org.junit.Test;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.InternalRepositoryException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class HgIncomingCommandTest extends IncomingOutgoingTestBase
{


  @Test
  public void testGetIncomingChangesets() throws IOException {
    writeNewFile(outgoing, outgoingDirectory, "a.txt", "Content of file a.txt");
    writeNewFile(outgoing, outgoingDirectory, "b.txt", "Content of file b.txt");

    Changeset c1 = commit(outgoing, "added a and b");

    writeNewFile(outgoing, outgoingDirectory, "c.txt", "Content of file c.txt");
    writeNewFile(outgoing, outgoingDirectory, "d.txt", "Content of file d.txt");

    Changeset c2 = commit(outgoing, "added c and d");

    HgIncomingCommand cmd = createIncomingCommand();
    IncomingCommandRequest request = new IncomingCommandRequest();

    request.setRemoteRepository(outgoingRepository);

    ChangesetPagingResult cpr = cmd.getIncomingChangesets(request);

    assertNotNull(cpr);
    assertEquals(2, cpr.getTotal());
    assertChangesetsEqual(c1, cpr.getChangesets().get(0));
    assertChangesetsEqual(c2, cpr.getChangesets().get(1));
  }

  @Test
  public void testGetIncomingChangesetsWithEmptyRepository() {
    HgIncomingCommand cmd = createIncomingCommand();
    IncomingCommandRequest request = new IncomingCommandRequest();

    request.setRemoteRepository(outgoingRepository);

    ChangesetPagingResult cpr = cmd.getIncomingChangesets(request);

    assertNotNull(cpr);
    assertEquals(0, cpr.getTotal());
  }

  @Test(expected = InternalRepositoryException.class)
  public void testGetIncomingChangesetsWithUnrelatedRepository() throws IOException {
    writeNewFile(outgoing, outgoingDirectory, "a.txt", "Content of file a.txt");
    writeNewFile(outgoing, outgoingDirectory, "b.txt", "Content of file b.txt");

    commit(outgoing, "added a and b");

    writeNewFile(incoming, incomingDirectory, "c.txt", "Content of file c.txt");
    writeNewFile(incoming, incomingDirectory, "d.txt", "Content of file d.txt");

    commit(incoming, "added c and d");

    HgIncomingCommand cmd = createIncomingCommand();
    IncomingCommandRequest request = new IncomingCommandRequest();

    request.setRemoteRepository(outgoingRepository);

    cmd.getIncomingChangesets(request);
  }

  private HgIncomingCommand createIncomingCommand() {
    HgConfigResolver resolver = new HgConfigResolver(handler);
    return new HgIncomingCommand(
      new HgCommandContext(resolver, HgTestUtil.createFactory(handler, incomingDirectory), incomingRepository),
      handler
    );
  }
}
