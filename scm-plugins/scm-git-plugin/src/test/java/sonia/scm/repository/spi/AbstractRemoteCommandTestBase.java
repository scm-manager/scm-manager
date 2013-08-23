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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Provider;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.eclipse.jgit.transport.Transport;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class AbstractRemoteCommandTestBase
{

  /**
   * Method description
   *
   *
   * @throws GitAPIException
   * @throws IOException
   */
  @Before
  public void setup() throws IOException, GitAPIException
  {
    incomingDirectory = tempFolder.newFile("incoming");
    incomingDirectory.delete();
    outgoingDirectory = tempFolder.newFile("outgoing");
    outgoingDirectory.delete();

    incomgingRepository = new Repository("1", "git", "incoming");
    outgoingRepository = new Repository("2", "git", "outgoing");

    incoming = Git.init().setDirectory(incomingDirectory).setBare(false).call();
    outgoing = Git.init().setDirectory(outgoingDirectory).setBare(false).call();

    handler = mock(GitRepositoryHandler.class);
    when(handler.getDirectory(incomgingRepository)).thenReturn(
      incomingDirectory);
    when(handler.getDirectory(outgoingRepository)).thenReturn(
      outgoingDirectory);
  }

  /**
   * Method description
   *
   */
  @After
  public void tearDownProtocol()
  {
    Transport.unregister(proto);
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
   * @param expected
   * @param actual
   */
  protected void assertCommitsEquals(RevCommit expected, Changeset actual)
  {
    assertEquals(expected.getId().name(), actual.getId());
    assertEquals(expected.getAuthorIdent().getName(),
      actual.getAuthor().getName());
    assertEquals(expected.getAuthorIdent().getEmailAddress(),
      actual.getAuthor().getMail());
    assertEquals(expected.getShortMessage(), actual.getDescription());
  }

  /**
   * Method description
   *
   *
   * @param git
   * @param message
   *
   * @return
   *
   * @throws GitAPIException
   */
  protected RevCommit commit(Git git, String message) throws GitAPIException
  {
    User trillian = UserTestData.createTrillian();
    CommitCommand cc = git.commit();

    cc.setAuthor(trillian.getDisplayName(), trillian.getMail());
    cc.setMessage(message);

    return cc.call();
  }

  /**
   * Method description
   *
   *
   * @param git
   * @param parent
   * @param name
   * @param content
   *
   * @throws GitAPIException
   * @throws IOException
   */
  protected void write(Git git, File parent, String name, String content)
    throws IOException, GitAPIException
  {
    File file = new File(parent, name);

    Files.write(content, file, Charsets.UTF_8);
    git.add().addFilepattern(name).call();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  /** Field description */
  protected GitRepositoryHandler handler;

  /** Field description */
  protected Repository incomgingRepository;

  /** Field description */
  protected Git incoming;

  /** Field description */
  protected File incomingDirectory;

  /** Field description */
  protected Git outgoing;

  /** Field description */
  protected File outgoingDirectory;

  /** Field description */
  protected Repository outgoingRepository;

  /** Field description */
  private ScmTransportProtocol proto;
}
