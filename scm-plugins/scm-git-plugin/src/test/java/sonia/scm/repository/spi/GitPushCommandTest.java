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

import com.google.inject.Provider;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.eclipse.jgit.transport.Transport;

import org.junit.Before;
import org.junit.Test;

import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.api.PushResponse;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Iterator;

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
   * @throws RepositoryException
   */
  @Test
  public void testPush()
    throws IOException, GitAPIException, RepositoryException
  {
    write(outgoing, outgoingDirectory, "a.txt", "content of a.txt");

    RevCommit o1 = commit(outgoing, "added a");

    write(outgoing, outgoingDirectory, "b.txt", "content of b.txt");

    RevCommit o2 = commit(outgoing, "added b");

    GitPushCommand cmd = createCommand();
    PushCommandRequest request = new PushCommandRequest();

    request.setRemoteRepository(incomgingRepository);

    PushResponse response = cmd.push(request);

    assertNotNull(response);
    assertEquals(2l, response.getChangesetCount());

    Iterator<RevCommit> commits = incoming.log().call().iterator();

    assertEquals(o2, commits.next());
    assertEquals(o1, commits.next());
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setUpProtocol()
  {

    // store reference to handle weak references
    proto = new ScmTransportProtocol(new Provider<HookEventFacade>()
    {

      @Override
      public HookEventFacade get()
      {
        return null;
      }
    }, new Provider<GitRepositoryHandler>()
    {

      @Override
      public GitRepositoryHandler get()
      {
        return null;
      }
    });
    Transport.register(proto);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private GitPushCommand createCommand()
  {
    return new GitPushCommand(handler, new GitContext(outgoingDirectory),
      outgoingRepository);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmTransportProtocol proto;
}
