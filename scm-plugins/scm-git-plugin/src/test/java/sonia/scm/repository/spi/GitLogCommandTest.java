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

import org.junit.Test;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.RepositoryException;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Unit tests for {@link GitLogCommand}.
 * 
 * @author Sebastian Sdorra
 */
public class GitLogCommandTest extends AbstractGitCommandTestBase
{

  /**
   * Tests log command with the usage of a default branch.
   * 
   * @throws IOException
   * @throws GitAPIException
   * @throws RepositoryException 
   */
  @Test
  public void testGetDefaultBranch() throws IOException, GitAPIException, RepositoryException {
    // without default branch, the repository head should be used
    ChangesetPagingResult result = createCommand().getChangesets(new LogCommandRequest());

    assertNotNull(result);
    assertEquals(4, result.getTotal());
    assertEquals("fcd0ef1831e4002ac43ea539f4094334c79ea9ec", result.getChangesets().get(0).getId());
    assertEquals("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1", result.getChangesets().get(1).getId());
    assertEquals("592d797cd36432e591416e8b2b98154f4f163411", result.getChangesets().get(2).getId());
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca", result.getChangesets().get(3).getId());
    
    // set default branch and fetch again
    repository.setProperty(AbstractGitCommand.PROPERTY_DEFAULT_BRANCH, "test-branch");
    
    result = createCommand().getChangesets(new LogCommandRequest());

    assertNotNull(result);
    assertEquals(3, result.getTotal());
    assertEquals("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4", result.getChangesets().get(0).getId());
    assertEquals("592d797cd36432e591416e8b2b98154f4f163411", result.getChangesets().get(1).getId());
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca", result.getChangesets().get(2).getId());
  }
  
  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetAll() throws IOException, RepositoryException
  {
    ChangesetPagingResult result =
      createCommand().getChangesets(new LogCommandRequest());

    assertNotNull(result);
    assertEquals(4, result.getTotal());
    assertEquals(4, result.getChangesets().size());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetAllByPath() throws IOException, RepositoryException
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setPath("a.txt");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(2, result.getTotal());
    assertEquals(2, result.getChangesets().size());
    assertEquals("fcd0ef1831e4002ac43ea539f4094334c79ea9ec", result.getChangesets().get(0).getId());
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca", result.getChangesets().get(1).getId());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetAllWithLimit() throws IOException, RepositoryException
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(4, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);

    assertNotNull(c1);
    assertEquals("fcd0ef1831e4002ac43ea539f4094334c79ea9ec", c1.getId());

    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c2);
    assertEquals("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1", c2.getId());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetAllWithPaging() throws IOException, RepositoryException
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(1);
    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(4, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);

    assertNotNull(c1);
    assertEquals("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1", c1.getId());

    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c2);
    assertEquals("592d797cd36432e591416e8b2b98154f4f163411", c2.getId());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetCommit()
  {
    GitLogCommand command = createCommand();
    Changeset c = command.getChangeset("435df2f061add3589cb3");

    assertNotNull(c);
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca", c.getId());
    assertEquals("added a and b files", c.getDescription());
    checkDate(c.getDate());
    assertEquals("Douglas Adams", c.getAuthor().getName());
    assertEquals("douglas.adams@hitchhiker.com", c.getAuthor().getMail());
    assertEquals("added a and b files", c.getDescription());

    Modifications mods = c.getModifications();

    assertNotNull(mods);
    assertTrue("modified list should be empty", mods.getModified().isEmpty());
    assertTrue("removed list should be empty", mods.getRemoved().isEmpty());
    assertFalse("added list should not be empty", mods.getAdded().isEmpty());
    assertEquals(2, mods.getAdded().size());
    assertThat(mods.getAdded(), contains("a.txt", "b.txt"));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetRange() throws IOException, RepositoryException
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("592d797cd36432e59141");
    request.setEndChangeset("435df2f061add3589cb3");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(2, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);
    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c1);
    assertEquals("592d797cd36432e591416e8b2b98154f4f163411", c1.getId());
    assertNotNull(c2);
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca", c2.getId());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private GitLogCommand createCommand()
  {
    return new GitLogCommand(createContext(), repository);
  }
}
