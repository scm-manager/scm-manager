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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.event.ScmEventBus;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.Collections.emptyList;

@Singleton
class RepositoryPostProcessor {

  private final ScmEventBus eventBus;

  private final Map<String, List<HealthCheckFailure>> checkResults = new HashMap<>();

  @Inject
  RepositoryPostProcessor(ScmEventBus eventBus) {
    this.eventBus = eventBus;
  }

  void setCheckResults(Repository repository, Collection<HealthCheckFailure> failures) {
    List<HealthCheckFailure> oldFailures = getCheckResults(repository.getId());
    List<HealthCheckFailure> copyOfFailures = copyOf(failures);
    checkResults.put(repository.getId(), copyOfFailures);
    repository.setHealthCheckFailures(copyOfFailures);
    eventBus.post(new HealthCheckEvent(repository, oldFailures, copyOfFailures));
  }

  void postProcess(Repository repository) {
    repository.setHealthCheckFailures(getCheckResults(repository.getId()));
  }

  private List<HealthCheckFailure> getCheckResults(String repositoryId) {
    return checkResults.getOrDefault(repositoryId, emptyList());
  }
}
