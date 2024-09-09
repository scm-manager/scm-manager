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
