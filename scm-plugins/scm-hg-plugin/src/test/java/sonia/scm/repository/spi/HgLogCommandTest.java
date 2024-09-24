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


import org.junit.Test;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Modifications;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class HgLogCommandTest extends AbstractHgCommandTestBase
{

  @Test
  public void testGetAll() {
    ChangesetPagingResult result =
      createCommand().getChangesets(new LogCommandRequest());

    assertNotNull(result);
    assertEquals(5, result.getTotal());
    assertEquals(5, result.getChangesets().size());
  }

  @Test
  public void testGetAllByPath() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPath("a.txt");

    ChangesetPagingResult result = createCommand().getChangesets(request);

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
  public void testGetDefaultBranchInfo() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPath("a.txt");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(1,
      result.getChangesets().get(0).getBranches().size());
    assertEquals("default",
      result.getChangesets().get(0).getBranches().get(0));
  }

  @Test
  public void testGetAllWithLimit() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

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

    ChangesetPagingResult result = createCommand().getChangesets(request);

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
  public void testGetCommit() throws IOException {
    HgLogCommand command = createCommand();
    String revision = "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427";
    Changeset c =
      command.getChangeset(revision, null);

    assertNotNull(c);
    assertEquals(revision, c.getId());
    assertEquals("added a and b files", c.getDescription());
    checkDate(c.getDate());
    assertEquals("Douglas Adams", c.getAuthor().getName());
    assertEquals("douglas.adams@hitchhiker.com", c.getAuthor().getMail());
    assertEquals("added a and b files", c.getDescription());
    ModificationsCommand modificationsCommand = new HgModificationsCommand(cmdContext);
    Modifications modifications = modificationsCommand.getModifications(revision);

    assertNotNull(modifications);
    assertTrue("modified list should be empty", modifications.getModified().isEmpty());
    assertTrue("removed list should be empty", modifications.getRemoved().isEmpty());
    assertFalse("added list should not be empty", modifications.getAdded().isEmpty());
    assertEquals(2, modifications.getAdded().size());
    assertThat(modifications.getAdded())
      .extracting("path")
      .containsExactly("a.txt", "b.txt");
  }

  @Test
  public void testGetRange() {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("3049df33fdbb");
    request.setEndChangeset("a9bacaf1b7fa");

    ChangesetPagingResult result = createCommand().getChangesets(request);

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

  
  private HgLogCommand createCommand()
  {
    return new HgLogCommand(cmdContext);
  }
}
