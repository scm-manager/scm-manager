/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.update;

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.RepositoryEvent;

import javax.inject.Inject;
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
