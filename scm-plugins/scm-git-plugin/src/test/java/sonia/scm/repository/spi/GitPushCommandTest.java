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
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.api.PushFailedException;
import sonia.scm.repository.api.PushResponse;

import java.io.IOException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


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


  private GitPushCommand createCommand()
  {
    return new GitPushCommand(handler, new GitContext(outgoingDirectory, outgoingRepository, null, new GitConfig()));
  }
}
