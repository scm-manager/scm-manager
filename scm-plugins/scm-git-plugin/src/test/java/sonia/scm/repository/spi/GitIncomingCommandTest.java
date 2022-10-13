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

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Ignore;
import org.junit.Test;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
public class GitIncomingCommandTest
  extends AbstractRemoteCommandTestBase {

  private final LfsLoader lfsLoader = mock(LfsLoader.class);

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
      lfsLoader);
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
