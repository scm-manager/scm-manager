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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HgLogCommandAncestorTest extends AbstractHgCommandTestBase {

  @Test
  public void testAncestorRange() {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("default");
    request.setAncestorChangeset("testbranch");

    ChangesetPagingResult result = new HgLogCommand(cmdContext).getChangesets(request);

    assertNotNull(result);
    assertEquals(3, result.getTotal());
    assertEquals(3, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);
    Changeset c2 = result.getChangesets().get(1);
    Changeset c3 = result.getChangesets().get(2);

    assertNotNull(c1);
    assertEquals("94dd2a4ebc27d30f811d7ac02fbd1cc382386bf3", c1.getId());
    assertNotNull(c2);
    assertEquals("aed7afe001a4d5d3111a5916a5656b3032eb4dc2", c2.getId());
    assertNotNull(c3);
    assertEquals("03a757fea8b21879d33944b710347c46aa4cfde1", c3.getId());

  }

  @Test
  public void testAncestorReverseRange() {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("testbranch");
    request.setAncestorChangeset("default");

    ChangesetPagingResult result = new HgLogCommand(cmdContext).getChangesets(request);

    assertNotNull(result);
    assertEquals(1, result.getTotal());
    assertEquals(1, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);

    assertNotNull(c1);
    assertEquals("d2bb8ada6ae68405627d2c757d9d656a4c21799f", c1.getId());
  }

  @Test
  public void testAncestorRangeWithPagination() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(1);
    request.setPagingLimit(2);
    request.setStartChangeset("default");
    request.setAncestorChangeset("testbranch");

    ChangesetPagingResult result = new HgLogCommand(cmdContext).getChangesets(request);

    assertNotNull(result);
    assertEquals(3, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);
    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c1);
    assertEquals("aed7afe001a4d5d3111a5916a5656b3032eb4dc2", c1.getId());
    assertNotNull(c2);
    assertEquals("03a757fea8b21879d33944b710347c46aa4cfde1", c2.getId());
  }

  @Test
  public void testAncestorReverseRangeWithPagination() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(0);
    request.setPagingLimit(2);
    request.setStartChangeset("testbranch");
    request.setAncestorChangeset("default");

    ChangesetPagingResult result = new HgLogCommand(cmdContext).getChangesets(request);

    assertNotNull(result);
    assertEquals(1, result.getTotal());
    assertEquals(1, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);

    assertNotNull(c1);
    assertEquals("d2bb8ada6ae68405627d2c757d9d656a4c21799f", c1.getId());
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-hg-ahead-behind-test.zip";
  }
}
