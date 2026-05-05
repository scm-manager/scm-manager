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

import org.junit.jupiter.api.Test;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HgLogCommandAncestorTest extends AbstractHgCommandTestBase {

  @Test
  public void testAncestorRange() {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("default");
    request.setAncestorChangeset("testbranch");

    ChangesetPagingResult result = new HgLogCommand(cmdContext).getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(3);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "94dd2a4ebc27d30f811d7ac02fbd1cc382386bf3",
        "aed7afe001a4d5d3111a5916a5656b3032eb4dc2",
        "03a757fea8b21879d33944b710347c46aa4cfde1"
      ));
  }

  @Test
  public void testAncestorReverseRange() {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("testbranch");
    request.setAncestorChangeset("default");

    ChangesetPagingResult result = new HgLogCommand(cmdContext).getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(1);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "d2bb8ada6ae68405627d2c757d9d656a4c21799f"
      ));
  }

  @Test
  public void testAncestorRangeWithPagination() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(1);
    request.setPagingLimit(2);
    request.setStartChangeset("default");
    request.setAncestorChangeset("testbranch");

    ChangesetPagingResult result = new HgLogCommand(cmdContext).getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(3);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "aed7afe001a4d5d3111a5916a5656b3032eb4dc2",
        "03a757fea8b21879d33944b710347c46aa4cfde1"
      ));
  }

  @Test
  public void testAncestorReverseRangeWithPagination() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(0);
    request.setPagingLimit(2);
    request.setStartChangeset("testbranch");
    request.setAncestorChangeset("default");

    ChangesetPagingResult result = new HgLogCommand(cmdContext).getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(1);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "d2bb8ada6ae68405627d2c757d9d656a4c21799f"
      ));
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-hg-ahead-behind-test.zip";
  }
}
