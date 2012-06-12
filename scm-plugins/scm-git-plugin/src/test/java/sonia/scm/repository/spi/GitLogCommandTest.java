/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitLogCommandTest extends AbstractGitCommandTestBase
{

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testGetAll() throws IOException
  {
    ChangesetPagingResult result = new GitLogCommand(
                                       repository,
                                       repositoryDirectory).getChangesets(
                                         new LogCommandRequest());

    assertNotNull(result);
    assertEquals(5, result.getTotal());
    assertEquals(5, result.getChangesets().size());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testGetAllByPath() throws IOException
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setPath("a.txt");

    ChangesetPagingResult result =
      new GitLogCommand(repository, repositoryDirectory).getChangesets(request);

    assertNotNull(result);
    assertEquals(3, result.getTotal());
    assertEquals(3, result.getChangesets().size());
    assertEquals("fcd0ef1831e4002ac43e", result.getChangesets().get(0).getId());
    assertEquals("3f76a12f08a6ba0dc988", result.getChangesets().get(1).getId());
    assertEquals("435df2f061add3589cb3", result.getChangesets().get(2).getId());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testGetAllWithLimit() throws IOException
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingLimit(2);

    ChangesetPagingResult result =
      new GitLogCommand(repository, repositoryDirectory).getChangesets(request);

    assertNotNull(result);
    assertEquals(5, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);

    assertNotNull(c1);
    assertEquals("fcd0ef1831e4002ac43e", c1.getId());

    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c2);
    assertEquals("86a6645eceefe8b9a247", c2.getId());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetCommit()
  {
    GitLogCommand command = new GitLogCommand(repository, repositoryDirectory);
    Changeset c = command.getChangeset("435df2f061add3589cb3");

    assertNotNull(c);
    assertEquals("435df2f061add3589cb3", c.getId());
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
   */
  @Test
  public void testGetRange() throws IOException
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("592d797cd36432e59141");
    request.setEndChangeset("435df2f061add3589cb3");

    ChangesetPagingResult result =
      new GitLogCommand(repository, repositoryDirectory).getChangesets(request);

    assertNotNull(result);
    assertEquals(2, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);
    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c1);
    assertEquals("592d797cd36432e59141", c1.getId());
    assertNotNull(c2);
    assertEquals("435df2f061add3589cb3", c2.getId());
  }
}
