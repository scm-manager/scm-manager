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
import sonia.scm.repository.RevisionNotFoundException;

import java.io.IOException;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class HgLogCommandTest extends AbstractHgCommandTestBase
{

  @Test
  public void testGetAll() {
    ChangesetPagingResult result =
      createComamnd().getChangesets(new LogCommandRequest());

    assertNotNull(result);
    assertEquals(5, result.getTotal());
    assertEquals(5, result.getChangesets().size());
  }

  @Test
  public void testGetAllByPath() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPath("a.txt");

    ChangesetPagingResult result = createComamnd().getChangesets(request);

    assertNotNull(result);
    assertEquals(3, result.getTotal());
    assertEquals(3, result.getChangesets().size());
    assertEquals("2baab8e80280ef05a9aa76c49c76feca2872afb7",
      result.getChangesets().get(0).getId());
    assertEquals("79b6baf49711ae675568e0698d730b97ef13e84a",
      result.getChangesets().get(1).getId());
    assertEquals("a9bacaf1b7fa0cebfca71fed4e59ed69a6319427",
      result.getChangesets().get(2).getId());
  }

  @Test
  public void testGetAllWithLimit() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingLimit(2);

    ChangesetPagingResult result = createComamnd().getChangesets(request);

    assertNotNull(result);
    assertEquals(5, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);

    assertNotNull(c1);
    assertEquals("2baab8e80280ef05a9aa76c49c76feca2872afb7", c1.getId());

    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c2);
    assertEquals("542bf4893dd2ff58a0eb719551d75ddeb919608b", c2.getId());
  }

  @Test
  public void testGetAllWithPaging() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(1);
    request.setPagingLimit(2);

    ChangesetPagingResult result = createComamnd().getChangesets(request);

    assertNotNull(result);
    assertEquals(5, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);

    assertNotNull(c1);
    assertEquals("542bf4893dd2ff58a0eb719551d75ddeb919608b", c1.getId());

    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c2);
    assertEquals("79b6baf49711ae675568e0698d730b97ef13e84a", c2.getId());
  }

  @Test
  public void testGetCommit() throws IOException, RevisionNotFoundException {
    HgLogCommand command = createComamnd();
    String revision = "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427";
    Changeset c =
      command.getChangeset(revision);

    assertNotNull(c);
    assertEquals(revision, c.getId());
    assertEquals("added a and b files", c.getDescription());
    checkDate(c.getDate());
    assertEquals("Douglas Adams", c.getAuthor().getName());
    assertEquals("douglas.adams@hitchhiker.com", c.getAuthor().getMail());
    assertEquals("added a and b files", c.getDescription());
    ModificationsCommand modificationsCommand = new HgModificationsCommand(cmdContext, repository);
    Modifications modifications = modificationsCommand.getModifications(revision);

    assertNotNull(modifications);
    assertTrue("modified list should be empty", modifications.getModified().isEmpty());
    assertTrue("removed list should be empty", modifications.getRemoved().isEmpty());
    assertFalse("added list should not be empty", modifications.getAdded().isEmpty());
    assertEquals(2, modifications.getAdded().size());
    assertThat(modifications.getAdded(), contains("a.txt", "b.txt"));
  }

  @Test
  public void testGetRange() {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("3049df33fdbb");
    request.setEndChangeset("a9bacaf1b7fa");

    ChangesetPagingResult result = createComamnd().getChangesets(request);

    assertNotNull(result);
    assertEquals(2, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);
    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c1);
    assertEquals("3049df33fdbbded08b707bac3eccd0f7b453c58b", c1.getId());
    assertNotNull(c2);
    assertEquals("a9bacaf1b7fa0cebfca71fed4e59ed69a6319427", c2.getId());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private HgLogCommand createComamnd()
  {
    return new HgLogCommand(cmdContext, repository);
  }
}
