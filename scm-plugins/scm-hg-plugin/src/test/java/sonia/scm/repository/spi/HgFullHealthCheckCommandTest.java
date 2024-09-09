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

import org.javahg.commands.ExecutionException;
import org.junit.Test;
import sonia.scm.repository.HealthCheckResult;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class HgFullHealthCheckCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void shouldDetectMissingFile() throws IOException {
    HgFullHealthCheckCommand checkCommand = new HgFullHealthCheckCommand(cmdContext);
    File d = new File(cmdContext.open().getDirectory(), ".hg/store/data/c/d.txt.i");
    d.delete();

    HealthCheckResult check = checkCommand.check();

    assertThat(check.isHealthy()).isFalse();
  }

  @Test
  public void shouldBeOkForValidRepository() throws IOException {
    HgFullHealthCheckCommand checkCommand = new HgFullHealthCheckCommand(cmdContext);

    HealthCheckResult check = checkCommand.check();

    assertThat(check.isHealthy()).isTrue();
  }
}
