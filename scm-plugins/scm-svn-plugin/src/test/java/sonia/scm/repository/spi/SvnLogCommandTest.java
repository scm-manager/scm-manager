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
import sonia.scm.repository.RepositoryException;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnLogCommandTest extends AbstractSvnCommandTestBase
{

  /**
   *   Method description
   *
   *
   *   @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetAll() throws IOException, RepositoryException
  {
    ChangesetPagingResult result =
      createCommand().getChangesets(new LogCommandRequest());

    assertNotNull(result);
    assertEquals(6, result.getTotal());
    assertEquals(6, result.getChangesets().size());
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
    assertEquals(3, result.getTotal());
    assertEquals(3, result.getChangesets().size());
    assertEquals("5", result.getChangesets().get(0).getId());
    assertEquals("3", result.getChangesets().get(1).getId());
    assertEquals("1", result.getChangesets().get(2).getId());
  }

  /**
   *  Method description
   *
   *
   *  @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetAllWithLimit() throws IOException, RepositoryException
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(6, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);

    assertNotNull(c1);
    assertEquals("5", c1.getId());

    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c2);
    assertEquals("4", c2.getId());
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
    assertEquals(6, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);

    assertNotNull(c1);
    assertEquals("4", c1.getId());

    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c2);
    assertEquals("3", c2.getId());
  }

  /**
   *  Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetCommit() throws IOException, RepositoryException
  {
    Changeset c = createCommand().getChangeset("3");

    assertNotNull(c);
    assertEquals("3", c.getId());
    assertEquals("remove b and modified a", c.getDescription());
    checkDate(c.getDate());
    assertEquals("perfect", c.getAuthor().getName());
    assertNull("douglas.adams@hitchhiker.com", c.getAuthor().getMail());

    Modifications mods = c.getModifications();

    assertNotNull(mods);
    assertEquals(1, mods.getModified().size());
    assertEquals(1, mods.getRemoved().size());
    assertTrue("added list should be empty", mods.getAdded().isEmpty());
    assertEquals("a.txt", mods.getModified().get(0));
    assertEquals("b.txt", mods.getRemoved().get(0));
  }

  /**
   *  Method description
   *
   *
   *  @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetRange() throws IOException, RepositoryException
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("2");
    request.setEndChangeset("1");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(2, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);
    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c1);
    assertEquals("2", c1.getId());
    assertNotNull(c2);
    assertEquals("1", c2.getId());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private SvnLogCommand createCommand()
  {
    return new SvnLogCommand(createContext(), repository);
  }
}
