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

import lombok.AllArgsConstructor;
import sonia.scm.event.Event;

import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;

/**
 * This event is triggered whenever a health check was run and either found issues
 * or issues reported earlier are fixed (that is, health has changed).
 *
 * @since 2.17.0
 */
@Event
@AllArgsConstructor
public class HealthCheckEvent {

  private final Repository repository;
  private final Collection<HealthCheckFailure> previousFailures;
  private final Collection<HealthCheckFailure> currentFailures;

  public Repository getRepository() {
    return repository;
  }

  public Collection<HealthCheckFailure> getPreviousFailures() {
    return unmodifiableCollection(previousFailures);
  }

  public Collection<HealthCheckFailure> getCurrentFailures() {
    return unmodifiableCollection(currentFailures);
  }
}
