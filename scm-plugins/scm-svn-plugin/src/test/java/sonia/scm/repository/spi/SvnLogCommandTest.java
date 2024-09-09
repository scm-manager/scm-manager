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


import com.google.common.collect.Iterables;
import org.junit.Test;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Modifications;

import java.util.stream.StreamSupport;

import static org.junit.Assert.*;


public class SvnLogCommandTest extends AbstractSvnCommandTestBase
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
  public void shouldNotReturnChangesetWithIdZero() {
    ChangesetPagingResult result = createCommand().getChangesets(new LogCommandRequest());
    boolean found = StreamSupport.stream(result.spliterator(), false).anyMatch(c -> "0".equals(c.getId()));
    assertFalse(found);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionForChangesetZero() {
    createCommand().getChangeset("0", new LogCommandRequest());
  }

  @Test
  public void shouldNotReturnChangesetZeroAsParent() {
    Changeset changeset = createCommand().getChangeset("1", new LogCommandRequest());
    assertTrue(changeset.getParents().isEmpty());
  }

  @Test
  public void shouldAppendParentChangeset() {
    Changeset changeset = createCommand().getChangeset("2", new LogCommandRequest());
    assertEquals(1, changeset.getParents().size());
    assertEquals("1", changeset.getParents().get(0));
  }

  @Test
  public void testShouldStartWithRevisionOne() {
    ChangesetPagingResult result = createCommand().getChangesets(new LogCommandRequest());
    Changeset first = Iterables.getLast(result);
    assertEquals("1", first.getId());
  }

  @Test
  public void testGetAllByPath() {
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
    assertEquals("5", c1.getId());

    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c2);
    assertEquals("4", c2.getId());
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
    assertEquals("4", c1.getId());

    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c2);
    assertEquals("3", c2.getId());
  }

  @Test
  public void testGetCommit() {
    Changeset c = createCommand().getChangeset("3", null);

    assertNotNull(c);
    assertEquals("3", c.getId());
    assertEquals("remove b and modified a", c.getDescription());
    checkDate(c.getDate());
    assertEquals("perfect", c.getAuthor().getName());
    assertNull("douglas.adams@hitchhiker.com", c.getAuthor().getMail());
    SvnModificationsCommand modificationsCommand = new SvnModificationsCommand(createContext());
    Modifications modifications = modificationsCommand.getModifications("3");

    assertNotNull(modifications);
    assertEquals(1, modifications.getModified().size());
    assertEquals(1, modifications.getRemoved().size());
    assertTrue("added list should be empty", modifications.getAdded().isEmpty());
    assertEquals("a.txt", modifications.getModified().get(0).getPath());
    assertEquals("b.txt", modifications.getRemoved().get(0).getPath());
  }

  @Test
  public void testGetRange() {
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


  private SvnLogCommand createCommand()
  {
    return new SvnLogCommand(createContext());
  }
}
