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


   @Before
  public void setUp()
  {
    setSubject(MockUtil.createAdminSubject());
  }



  protected void assertChangesetsEqual(Changeset expected,
    sonia.scm.repository.Changeset actual)
  {
    assertEquals(expected.getNode(), actual.getId());
    assertEquals(expected.getMessage(), actual.getDescription());
    assertEquals(expected.getUser(), actual.getAuthor().toString());
  }


  protected Changeset commit(BaseRepository repository, String message)
  {
    CommitCommand c = CommitCommand.on(repository);

    c.user(createUser(UserTestData.createTrillian()));
    c.message(message);

    return c.execute();
  }


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

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  protected HgRepositoryHandler handler;

  protected BaseRepository incoming;

  protected File incomingDirectory;

  protected sonia.scm.repository.Repository incomingRepository;

  protected BaseRepository outgoing;

  protected File outgoingDirectory;

  protected sonia.scm.repository.Repository outgoingRepository;
}
