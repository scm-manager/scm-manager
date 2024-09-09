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


import org.javahg.Changeset;
import org.junit.Test;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.InternalRepositoryException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class HgOutgoingCommandTest extends IncomingOutgoingTestBase
{

  @Test
  public void testGetOutgoingChangesets() throws IOException {
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

  @Test
  public void testGetOutgoingChangesetsWithEmptyRepository() {
    HgOutgoingCommand cmd = createOutgoingCommand();
    OutgoingCommandRequest request = new OutgoingCommandRequest();

    request.setRemoteRepository(outgoingRepository);

    ChangesetPagingResult cpr = cmd.getOutgoingChangesets(request);

    assertNotNull(cpr);
    assertEquals(0, cpr.getTotal());
  }

  @Test(expected = InternalRepositoryException.class)
  public void testGetOutgoingChangesetsWithUnrelatedRepository() throws IOException {
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

  private HgOutgoingCommand createOutgoingCommand() {
    HgConfigResolver resolver = new HgConfigResolver(handler);
    return new HgOutgoingCommand(
      new HgCommandContext(resolver, HgTestUtil.createFactory(handler, outgoingDirectory), outgoingRepository),
      handler
    );
  }
}
