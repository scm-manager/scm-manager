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

package sonia.scm.repository;

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import sonia.scm.plugin.Extension;

import java.time.Clock;
import java.time.Year;

@Extension
public class CurrentYearNamespaceStrategy implements NamespaceStrategy {

  private final Clock clock;

  @Inject
  public CurrentYearNamespaceStrategy() {
    this(Clock.systemDefaultZone());
  }

  @VisibleForTesting
  CurrentYearNamespaceStrategy(Clock clock) {
    this.clock = clock;
  }

  @Override
  public String createNamespace(Repository repository) {
    return String.valueOf(Year.now(clock).getValue());
  }
}
