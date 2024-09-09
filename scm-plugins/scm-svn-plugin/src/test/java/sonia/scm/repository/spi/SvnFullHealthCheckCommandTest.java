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
import sonia.scm.repository.HealthCheckResult;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class SvnFullHealthCheckCommandTest extends AbstractSvnCommandTestBase {

  @Test
  public void shouldBeOkForValidRepository() {
    HealthCheckResult check = new SvnFullHealthCheckCommand(createContext()).check();

    assertThat(check.isHealthy()).isTrue();
  }

  @Test
  public void shouldDetectMissingFile() {
    File revision4 = new File(createContext().getDirectory(), "db/revs/0/4");
    revision4.delete();

    HealthCheckResult check = new SvnFullHealthCheckCommand(createContext()).check();

    assertThat(check.isHealthy()).isFalse();
  }
}
