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

import org.junit.Test;

import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.RepositoryException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgOutgoingCommandTest extends IncomingOutgoingTestBase
{

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetOutgoingChangesets()
    throws IOException, RepositoryException
  {
    writeNewFile(outgoing, outgoingDirectory, "a.txt", "Content of file a.txt");
    writeNewFile(outgoing, outgoingDirectory, "b.txt", "Content of file b.txt");

    Changeset c1 = commit(outgoing, "added a and b");

    writeNewFile(outgoing, outgoingDirectory, "c.txt", "Content of file c.txt");
    writeNewFile(outgoing, outgoingDirectory, "d.txt", "Content of file d.txt");

    Changeset c2 = commit(outgoing, "added c and d");

    HgOutgoingCommand cmd = createOutgoingCommand();
    OutgoingCommandRequest request = new OutgoingCommandRequest();

    request.setRemoteRepository(incomingRepository);

    ChangesetPagingResult cpr = cmd.getOutgoingChangesets(request);

    assertNotNull(cpr);
    assertEquals(2, cpr.getTotal());
    assertChangesetsEqual(c1, cpr.getChangesets().get(0));
    assertChangesetsEqual(c2, cpr.getChangesets().get(1));
  }

  /**
   * Method description
   *
   *
   * @throws RepositoryException
   */
  @Test
  public void testGetOutgoingChangesetsWithEmptyRepository()
    throws RepositoryException
  {
    HgOutgoingCommand cmd = createOutgoingCommand();
    OutgoingCommandRequest request = new OutgoingCommandRequest();

    request.setRemoteRepository(outgoingRepository);

    ChangesetPagingResult cpr = cmd.getOutgoingChangesets(request);

    assertNotNull(cpr);
    assertEquals(0, cpr.getTotal());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test(expected = RepositoryException.class)
  public void testGetOutgoingChangesetsWithUnrelatedRepository()
    throws IOException, RepositoryException
  {
    writeNewFile(outgoing, outgoingDirectory, "a.txt", "Content of file a.txt");
    writeNewFile(outgoing, outgoingDirectory, "b.txt", "Content of file b.txt");

    commit(outgoing, "added a and b");

    writeNewFile(incoming, incomingDirectory, "c.txt", "Content of file c.txt");
    writeNewFile(incoming, incomingDirectory, "d.txt", "Content of file d.txt");

    commit(incoming, "added c and d");

    HgOutgoingCommand cmd = createOutgoingCommand();
    OutgoingCommandRequest request = new OutgoingCommandRequest();

    request.setRemoteRepository(incomingRepository);

    ChangesetPagingResult cpr = cmd.getOutgoingChangesets(request);

    System.out.println(cpr.getChangesets());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private HgOutgoingCommand createOutgoingCommand()
  {
    return new HgOutgoingCommand(
      new HgCommandContext(
        HgTestUtil.createHookManager(), handler, outgoingRepository,
          outgoingDirectory), outgoingRepository, handler);
  }
}
