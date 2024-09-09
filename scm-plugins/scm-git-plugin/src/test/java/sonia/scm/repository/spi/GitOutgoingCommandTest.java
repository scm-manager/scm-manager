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


import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link OutgoingCommand}.
 *
 */
public class GitOutgoingCommandTest extends AbstractRemoteCommandTestBase
{

  /**
   * Method description
   *
   *
   * @throws GitAPIException
   * @throws IOException
   */
  @Test
  public void testGetOutgoingChangesets()
    throws IOException, GitAPIException
  {
    write(outgoing, outgoingDirectory, "a.txt", "content of a.txt");

    RevCommit c1 = commit(outgoing, "added a");

    write(outgoing, outgoingDirectory, "b.txt", "content of b.txt");

    RevCommit c2 = commit(outgoing, "added b");

    GitOutgoingCommand cmd = createCommand();
    OutgoingCommandRequest request = new OutgoingCommandRequest();

    request.setRemoteRepository(incomingRepository);

    ChangesetPagingResult cpr = cmd.getOutgoingChangesets(request);

    assertNotNull(cpr);

    assertEquals(2, cpr.getTotal());
    assertCommitsEquals(c1, cpr.getChangesets().get(0));
    assertCommitsEquals(c2, cpr.getChangesets().get(1));
  }

  /**
   * Method description
   *
   *
   * @throws GitAPIException
   * @throws IOException
   */
  @Test
  public void testGetOutgoingChangesetsWithAlreadyPushedChanges()
    throws IOException, GitAPIException
  {
    write(outgoing, outgoingDirectory, "a.txt", "content of a.txt");

    commit(outgoing, "added a");

    GitPushCommand push = new GitPushCommand(handler,
      new GitContext(outgoingDirectory, outgoingRepository, null, new GitConfig())
    );
    PushCommandRequest req = new PushCommandRequest();

    req.setRemoteRepository(incomingRepository);
    push.push(req);

    write(outgoing, outgoingDirectory, "b.txt", "content of b.txt");

    RevCommit c2 = commit(outgoing, "added b");

    GitOutgoingCommand cmd = createCommand();
    OutgoingCommandRequest request = new OutgoingCommandRequest();

    request.setRemoteRepository(incomingRepository);

    ChangesetPagingResult cpr = cmd.getOutgoingChangesets(request);

    assertNotNull(cpr);

    assertEquals(1, cpr.getTotal());
    assertCommitsEquals(c2, cpr.getChangesets().get(0));
  }


  @Test
  public void testGetOutgoingChangesetsWithEmptyRepository()
    throws IOException
  {
    GitOutgoingCommand cmd = createCommand();
    OutgoingCommandRequest request = new OutgoingCommandRequest();

    request.setRemoteRepository(incomingRepository);

    ChangesetPagingResult cpr = cmd.getOutgoingChangesets(request);

    assertNotNull(cpr);

    assertEquals(0, cpr.getTotal());
  }

  
  private GitOutgoingCommand createCommand()
  {
    return new GitOutgoingCommand(
      new GitContext(outgoingDirectory, outgoingRepository, new GitRepositoryConfigStoreProvider(new InMemoryConfigurationStoreFactory()), new GitConfig()),
      handler,
      GitTestHelper.createConverterFactory()
    );
  }
}
