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

import org.javahg.BaseRepository;
import org.javahg.Changeset;
import org.javahg.Repository;
import org.javahg.RepositoryConfiguration;
import org.javahg.commands.AddCommand;
import org.javahg.commands.CommitCommand;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import sonia.scm.AbstractTestBase;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.util.MockUtil;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class IncomingOutgoingTestBase extends AbstractTestBase
{

  /**
   * Method description
   *
   * @throws IOException
   */
  @Before
  public void initHgHandler() throws IOException {
    HgRepositoryHandler temp = HgTestUtil.createHandler(tempFolder.newFolder());

    HgTestUtil.checkForSkip(temp);

    incomingDirectory = tempFolder.newFolder("incoming");
    outgoingDirectory = tempFolder.newFolder("outgoing");

    incomingRepository = new sonia.scm.repository.Repository("1", "hg", "space", "incoming");
    outgoingRepository = new sonia.scm.repository.Repository("2", "hg", "space", "outgoing");

    incoming = Repository.create(createConfig(temp), incomingDirectory);
    outgoing = Repository.create(createConfig(temp), outgoingDirectory);

    handler = mock(HgRepositoryHandler.class);
    when(handler.getDirectory(incomingRepository.getId())).thenReturn(
      incomingDirectory);
    when(handler.getDirectory(outgoingRepository.getId())).thenReturn(
      outgoingDirectory);
    when(handler.getConfig()).thenReturn(temp.getConfig());
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
    HgGlobalConfig cfg = handler.getConfig();
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
