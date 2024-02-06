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


import com.google.common.base.Charsets;
import com.google.common.io.Files;
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
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;


public class AbstractRemoteCommandTestBase {

  /**
   * Method description
   *
   * @throws GitAPIException
   * @throws IOException
   */
  @Before
  public void setup() throws IOException, GitAPIException {
    incomingDirectory = tempFolder.newFile("incoming");
    incomingDirectory.delete();
    outgoingDirectory = tempFolder.newFile("outgoing");
    outgoingDirectory.delete();

    incomingRepository = new Repository("1", "git", "space", "incoming");
    outgoingRepository = new Repository("2", "git", "space", "outgoing");

    incoming = Git.init().setDirectory(incomingDirectory).setBare(false).call();
    outgoing = Git.init().setDirectory(outgoingDirectory).setBare(false).call();

    eventBus = mock(ScmEventBus.class);
    eventFactory = mock(GitRepositoryHookEventFactory.class);

    handler = mock(GitRepositoryHandler.class);
    lenient().when(handler.getDirectory(incomingRepository.getId())).thenReturn(
      incomingDirectory);
    lenient().when(handler.getDirectory(outgoingRepository.getId())).thenReturn(
      outgoingDirectory);
  }

  /**
   * Method description
   */
  @After
  public void tearDownProtocol() {
    Transport.unregister(proto);
  }


  /**
   * Method description
   */
  @Before
  public void setUpProtocol() {

    // store reference to handle weak references
    proto = new ScmTransportProtocol(GitTestHelper::createConverterFactory, () -> null, () -> null);
    Transport.register(proto);
  }


  /**
   * Method description
   *
   * @param expected
   * @param actual
   */
  protected void assertCommitsEquals(RevCommit expected, Changeset actual) {
    assertEquals(expected.getId().name(), actual.getId());
    assertEquals(expected.getAuthorIdent().getName(),
      actual.getAuthor().getName());
    assertEquals(expected.getAuthorIdent().getEmailAddress(),
      actual.getAuthor().getMail());
    assertEquals(expected.getShortMessage(), actual.getDescription());
  }


  protected RevCommit commit(Git git, String message) throws GitAPIException {
    User trillian = UserTestData.createTrillian();
    CommitCommand cc = git.commit();

    cc.setAuthor(trillian.getDisplayName(), trillian.getMail());
    cc.setMessage(message);

    return cc.call();
  }

  /**
   * Method description
   *
   * @param git
   * @param parent
   * @param name
   * @param content
   * @throws GitAPIException
   * @throws IOException
   */
  protected void write(Git git, File parent, String name, String content)
    throws IOException, GitAPIException {
    File file = new File(parent, name);

    Files.write(content, file, Charsets.UTF_8);
    git.add().addFilepattern(name).call();
  }

  //~--- fields ---------------------------------------------------------------

  
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  
  protected GitRepositoryHandler handler;

  
  protected Repository incomingRepository;

  
  protected Git incoming;

  
  protected File incomingDirectory;

  
  protected Git outgoing;

  
  protected File outgoingDirectory;

  
  protected Repository outgoingRepository;

  
  private ScmTransportProtocol proto;

  protected ScmEventBus eventBus;
  protected GitRepositoryHookEventFactory eventFactory;
}
