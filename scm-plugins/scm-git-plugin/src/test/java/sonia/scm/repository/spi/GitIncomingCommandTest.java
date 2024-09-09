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
import org.junit.Ignore;
import org.junit.Test;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitHeadModifier;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;


public class GitIncomingCommandTest
  extends AbstractRemoteCommandTestBase {

  /**
   * Method description
   *
   * @throws GitAPIException
   * @throws IOException
   */
  @Test
  public void testGetIncomingChangesets()
    throws IOException, GitAPIException {
    write(outgoing, outgoingDirectory, "a.txt", "content of a.txt");

    RevCommit c1 = commit(outgoing, "added a");

    write(outgoing, outgoingDirectory, "b.txt", "content of b.txt");

    RevCommit c2 = commit(outgoing, "added b");

    GitIncomingCommand cmd = createCommand();
    IncomingCommandRequest request = new IncomingCommandRequest();

    request.setRemoteRepository(outgoingRepository);

    ChangesetPagingResult cpr = cmd.getIncomingChangesets(request);

    assertNotNull(cpr);

    assertEquals(2, cpr.getTotal());
    assertCommitsEquals(c1, cpr.getChangesets().get(0));
    assertCommitsEquals(c2, cpr.getChangesets().get(1));
  }

  /**
   * Method description
   *
   * @throws GitAPIException
   * @throws IOException
   */
  @Test
  public void testGetIncomingChangesetsWithAlreadyPulledChangesets()
    throws IOException, GitAPIException {
    write(outgoing, outgoingDirectory, "a.txt", "content of a.txt");

    commit(outgoing, "added a");

    GitContext context = new GitContext(incomingDirectory, incomingRepository, new GitRepositoryConfigStoreProvider(new InMemoryConfigurationStoreFactory()), new GitConfig());
    PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory = new PostReceiveRepositoryHookEventFactory(eventBus, eventFactory, context);

    GitPullCommand pull = new GitPullCommand(
      handler,
      context,
      postReceiveRepositoryHookEventFactory,
      mock(LfsLoader.class),
      mock(PullHttpConnectionProvider.class),
      mock(GitRepositoryConfigStoreProvider.class),
      mock(GitHeadModifier.class));
    PullCommandRequest req = new PullCommandRequest();
    req.setRemoteRepository(outgoingRepository);
    pull.pull(req);

    write(outgoing, outgoingDirectory, "b.txt", "content of b.txt");

    RevCommit c2 = commit(outgoing, "added b");

    GitIncomingCommand cmd = createCommand();
    IncomingCommandRequest request = new IncomingCommandRequest();

    request.setRemoteRepository(outgoingRepository);

    ChangesetPagingResult cpr = cmd.getIncomingChangesets(request);

    assertNotNull(cpr);

    assertEquals(1, cpr.getTotal());
    assertCommitsEquals(c2, cpr.getChangesets().get(0));
  }

  /**
   * Method description
   *
   * @throws IOException
   */
  @Test
  public void testGetIncomingChangesetsWithEmptyRepository()
    throws IOException {
    GitIncomingCommand cmd = createCommand();
    IncomingCommandRequest request = new IncomingCommandRequest();

    request.setRemoteRepository(outgoingRepository);

    ChangesetPagingResult cpr = cmd.getIncomingChangesets(request);

    assertNotNull(cpr);

    assertEquals(0, cpr.getTotal());
  }

  /**
   * Check for correct behaviour
   *
   * @throws GitAPIException
   * @throws IOException
   */
  @Test
  @Ignore
  public void testGetIncomingChangesetsWithUnrelatedRepository()
    throws IOException, GitAPIException {
    write(outgoing, outgoingDirectory, "a.txt", "content of a.txt");

    commit(outgoing, "added a");

    write(incoming, incomingDirectory, "b.txt", "content of b.txt");

    commit(incoming, "added b");

    GitIncomingCommand cmd = createCommand();
    IncomingCommandRequest request = new IncomingCommandRequest();

    request.setRemoteRepository(outgoingRepository);

    ChangesetPagingResult cpr = cmd.getIncomingChangesets(request);

    assertNotNull(cpr);

    assertEquals(0, cpr.getTotal());
  }

  private GitIncomingCommand createCommand() {
    return new GitIncomingCommand(
      new GitContext(incomingDirectory, incomingRepository, new GitRepositoryConfigStoreProvider(new InMemoryConfigurationStoreFactory()), new GitConfig()),
      handler,
      GitTestHelper.createConverterFactory()
    );
  }
}
