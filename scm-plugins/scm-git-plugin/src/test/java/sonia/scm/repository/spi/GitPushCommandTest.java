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
import org.junit.Test;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.api.PushFailedException;
import sonia.scm.repository.api.PushResponse;

import java.io.IOException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class GitPushCommandTest extends AbstractRemoteCommandTestBase
{

  /**
   * Method description
   *
   *
   * @throws GitAPIException
   * @throws IOException
   */
  @Test
  public void testPush()
    throws IOException, GitAPIException
  {
    write(outgoing, outgoingDirectory, "a.txt", "content of a.txt");

    RevCommit o1 = commit(outgoing, "added a");

    write(outgoing, outgoingDirectory, "b.txt", "content of b.txt");

    RevCommit o2 = commit(outgoing, "added b");

    GitPushCommand cmd = createCommand();
    PushCommandRequest request = new PushCommandRequest();

    request.setRemoteRepository(incomingRepository);

    PushResponse response = cmd.push(request);

    assertNotNull(response);
    assertEquals(2L, response.getChangesetCount());

    Iterator<RevCommit> commits = incoming.log().call().iterator();

    assertEquals(o2, commits.next());
    assertEquals(o1, commits.next());
  }

  @Test
  public void testForcePush() throws IOException, GitAPIException {
    write(outgoing, outgoingDirectory, "a.txt", "content of a.txt");
    RevCommit outgoingCommit = commit(outgoing, "added a");

    write(incoming, incomingDirectory, "a.txt", "conflicting change of a.txt");
    commit(incoming, "changed a");

    PushCommandRequest request = new PushCommandRequest();
    request.setRemoteRepository(incomingRepository);
    request.setForce(true);

    GitPushCommand cmd = createCommand();
    PushResponse response = cmd.push(request);

    assertNotNull(response);
    assertEquals(0L, response.getChangesetCount());

    Iterator<RevCommit> commits = incoming.log().call().iterator();

    assertEquals(outgoingCommit, commits.next());
    assertThat(commits.hasNext()).isFalse();
  }

  @Test
  public void testFailedPushBecauseOfConflict() throws IOException, GitAPIException {
    write(outgoing, outgoingDirectory, "a.txt", "content of a.txt");
    commit(outgoing, "added a");

    write(incoming, incomingDirectory, "a.txt", "conflicting change of a.txt");
    RevCommit incomingCommit = commit(incoming, "changed a");

    GitPushCommand cmd = createCommand();
    PushCommandRequest request = new PushCommandRequest();
    request.setRemoteRepository(incomingRepository);

    assertThatThrownBy(() -> cmd.push(request))
      .isInstanceOf(PushFailedException.class)
      .hasMessageContaining("Failed to push");

    Iterator<RevCommit> commits = incoming.log().call().iterator();
    assertEquals(incomingCommit, commits.next());
    assertThat(commits.hasNext()).isFalse();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private GitPushCommand createCommand()
  {
    return new GitPushCommand(handler, new GitContext(outgoingDirectory, outgoingRepository, null, new GitConfig()));
  }
}
