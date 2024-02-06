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
