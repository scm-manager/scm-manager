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

import com.aragost.javahg.BaseRepository;
import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.RepositoryConfiguration;
import com.aragost.javahg.commands.AddCommand;
import com.aragost.javahg.commands.CommitCommand;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import sonia.scm.AbstractTestBase;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgContext;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.util.MockUtil;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class IncomingOutgoingTestBase extends AbstractTestBase
{

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Before
  public void initHgHandler() throws IOException
  {
    HgRepositoryHandler temp = HgTestUtil.createHandler(tempFolder.newFolder());

    HgTestUtil.checkForSkip(temp);

    incomingDirectory = tempFolder.newFolder("incoming");
    outgoingDirectory = tempFolder.newFolder("outgoing");

    incomingRepository = new sonia.scm.repository.Repository("1", "hg",
      "incoming");
    outgoingRepository = new sonia.scm.repository.Repository("2", "hg",
      "outgoing");

    incoming = Repository.create(createConfig(temp), incomingDirectory);
    outgoing = Repository.create(createConfig(temp), outgoingDirectory);

    handler = mock(HgRepositoryHandler.class);
    when(handler.getDirectory(incomingRepository)).thenReturn(
      incomingDirectory);
    when(handler.getDirectory(outgoingRepository)).thenReturn(
      outgoingDirectory);
    when(handler.getConfig()).thenReturn(temp.getConfig());
    when(handler.getHgContext()).thenReturn(new HgContext());
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setUp()
  {
    setSubject(MockUtil.createAdminSubject());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param expected
   * @param actual
   */
  protected void assertChangesetsEqual(Changeset expected,
    sonia.scm.repository.Changeset actual)
  {
    assertEquals(expected.getNode(), actual.getId());
    assertEquals(expected.getMessage(), actual.getDescription());
    assertEquals(expected.getUser(), actual.getAuthor().toString());
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param message
   *
   * @return
   */
  protected Changeset commit(BaseRepository repository, String message)
  {
    CommitCommand c = CommitCommand.on(repository);

    c.user(createUser(UserTestData.createTrillian()));
    c.message(message);

    return c.execute();
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @return
   */
  protected String createUser(User user)
  {
    return user.getDisplayName().concat(" <").concat(user.getMail()).concat(
      ">");
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param parent
   * @param name
   * @param content
   *
   * @throws IOException
   */
  protected void writeNewFile(BaseRepository repository, File parent,
    String name, String content)
    throws IOException
  {
    File file = new File(parent, name);

    Files.write(content, file, Charsets.UTF_8);
    AddCommand.on(repository).execute(file);
  }

  /**
   * Method description
   *
   *
   *
   * @param handler
   * @return
   */
  private RepositoryConfiguration createConfig(HgRepositoryHandler handler)
  {
    HgConfig cfg = handler.getConfig();
    RepositoryConfiguration configuration = RepositoryConfiguration.DEFAULT;

    configuration.setHgBin(cfg.getHgBinary());

    return configuration;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  /** Field description */
  protected HgRepositoryHandler handler;

  /** Field description */
  protected BaseRepository incoming;

  /** Field description */
  protected File incomingDirectory;

  /** Field description */
  protected sonia.scm.repository.Repository incomingRepository;

  /** Field description */
  protected BaseRepository outgoing;

  /** Field description */
  protected File outgoingDirectory;

  /** Field description */
  protected sonia.scm.repository.Repository outgoingRepository;
}
