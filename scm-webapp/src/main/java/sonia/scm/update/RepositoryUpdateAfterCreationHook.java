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

package sonia.scm.update;

import com.github.legman.Subscribe;
import jakarta.inject.Inject;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.RepositoryEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Extension @EagerSingleton
class RepositoryUpdateAfterCreationHook {

  private final List<RepositoryUpdateStep> repositorySteps;
  private final UpdateStepStore updateStepStore;

  @Inject
  RepositoryUpdateAfterCreationHook(Set<RepositoryUpdateStep> repositorySteps, UpdateStepStore updateStepStore) {
    this.repositorySteps = determineStepsWithHighestVersion(repositorySteps);
    this.updateStepStore = updateStepStore;
  }

  private ArrayList<RepositoryUpdateStep> determineStepsWithHighestVersion(Set<RepositoryUpdateStep> repositorySteps) {
    List<RepositoryUpdateStep> temporarySteps = new ArrayList<>(repositorySteps);
    temporarySteps.sort(Comparator.comparing(RepositoryUpdateStep::getTargetVersion).reversed());
    Map<String, RepositoryUpdateStep> stepsForDataType = new HashMap<>();
    temporarySteps.forEach(step -> stepsForDataType.put(step.getAffectedDataType(), step));
    return new ArrayList<>(stepsForDataType.values());
  }

  @Subscribe
  public void updateRepository(RepositoryEvent event) {
    if (event.getEventType() == HandlerEventType.CREATE) {
      repositorySteps.forEach(updateStep -> updateStepStore.storeExecutedUpdate(event.getItem().getId(), updateStep));
    }
  }
}
