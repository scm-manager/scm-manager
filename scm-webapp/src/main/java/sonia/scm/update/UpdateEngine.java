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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.migration.UpdateException;
import sonia.scm.migration.UpdateStep;
import sonia.scm.migration.UpdateStepTarget;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

public class UpdateEngine {

  public static final Logger LOG = LoggerFactory.getLogger(UpdateEngine.class);

  private static final String STORE_NAME = "executedUpdates";

  private final List<UpdateStepWrapper> steps;
  private final ConfigurationEntryStore<UpdateVersionInfo> store;
  private final ConfigurationEntryStoreFactory storeFactory;
  private final RepositoryUpdateIterator repositoryUpdateIterator;

  @Inject
  public UpdateEngine(
    Set<UpdateStep> globalSteps,
    Set<RepositoryUpdateStep> repositorySteps,
    ConfigurationEntryStoreFactory storeFactory,
    RepositoryUpdateIterator repositoryUpdateIterator
  ) {
    this.storeFactory = storeFactory;
    this.repositoryUpdateIterator = repositoryUpdateIterator;
    this.store = storeFactory.withType(UpdateVersionInfo.class).withName(STORE_NAME).build();
    this.steps = sortSteps(globalSteps, repositorySteps);
  }

  private List<UpdateStepWrapper> sortSteps(Set<UpdateStep> globalSteps, Set<RepositoryUpdateStep> repositorySteps) {
    LOG.trace("sorting available update steps:");
    List<UpdateStepWrapper> sortedSteps =
      concat(
        globalSteps.stream().filter(this::notRunYet).map(this::wrap),
        repositorySteps.stream().map(this::wrap))
      .sorted(
        Comparator
          .comparing(UpdateStepWrapper::getTargetVersion)
          .thenComparing(UpdateStepWrapper::isGlobalUpdateStep)
          .thenComparing(UpdateEngine::isCoreUpdateStep)
          .reversed()
      )
      .collect(toList());
    sortedSteps.forEach(step -> LOG.trace("{} for version {}", step.getAffectedDataType(), step.getTargetVersion()));
    return sortedSteps;
  }

  private UpdateStepWrapper wrap(UpdateStep updateStep) {
    return new UpdateStepWrapper(updateStep) {
      @Override
      public void doUpdate() throws Exception {
        LOG.info("running update step for type {} and version {} (class {})",
          updateStep.getAffectedDataType(),
          updateStep.getTargetVersion(),
          updateStep.getClass().getName()
        );
        updateStep.doUpdate();
        storeNewVersion(store, updateStep);
      }
    };
  }

  private UpdateStepWrapper wrap(RepositoryUpdateStep repositoryUpdateStep) {
    return new RepositoryUpdateStepWrapper(repositoryUpdateStep);
  }

  private void doUpdateForRepository(String repositoryId, RepositoryUpdateStep repositoryUpdateStep) throws Exception {
    if (notRunYet(repositoryUpdateStep, repositoryId)) {
      LOG.info("running update step for type {} and version {} (class {}) for repository id {}",
        repositoryUpdateStep.getAffectedDataType(),
        repositoryUpdateStep.getTargetVersion(),
        repositoryUpdateStep.getClass().getName(),
        repositoryId
      );
      repositoryUpdateStep.doUpdate(new RepositoryUpdateContext(repositoryId));
      storeNewVersion(storeForRepository(repositoryId), repositoryUpdateStep);
    }
  }

  private static boolean isCoreUpdateStep(UpdateStepWrapper updateStep) {
    return updateStep.isCoreUpdate();
  }

  public void update() {
    steps.forEach(this::execute);
  }

  public void update(String repsitoryId) {
    steps.forEach(step -> execute(step, repsitoryId));
  }

  private void execute(UpdateStepWrapper updateStep) {
    try {
      updateStep.doUpdate();
    } catch (Exception e) {
      throw new UpdateException(
        format(
          "could not execute update for type %s to version %s in %s",
          updateStep.getAffectedDataType(),
          updateStep.getTargetVersion(),
          updateStep.getClass()),
        e);
    }
  }

  private void execute(UpdateStepWrapper updateStep, String repositoryId) {
    try {
      updateStep.doUpdate(repositoryId);
    } catch (Exception e) {
      throw new UpdateException(
        format(
          "could not execute update for type %s to version %s in %s for repository id %s",
          updateStep.getAffectedDataType(),
          updateStep.getTargetVersion(),
          updateStep.getClass(),
          repositoryId),
        e);
    }
  }

  private void storeNewVersion(ConfigurationEntryStore<UpdateVersionInfo> store, UpdateStepTarget updateStep) {
    UpdateVersionInfo newVersionInfo = new UpdateVersionInfo(updateStep.getTargetVersion().getParsedVersion());
    store.put(updateStep.getAffectedDataType(), newVersionInfo);
  }

  private boolean notRunYet(UpdateStep updateStep) {
    LOG.trace("checking whether to run update step for type {} and version {}",
      updateStep.getAffectedDataType(),
      updateStep.getTargetVersion()
    );
    ConfigurationEntryStore<UpdateVersionInfo> store = this.store;
    return notRunYet(store, updateStep);
  }

  private boolean notRunYet(RepositoryUpdateStep repositoryUpdateStep, String repositoryId) {
    LOG.trace("checking whether to run update step for type {} and version {} on repository id {}",
      repositoryUpdateStep.getAffectedDataType(),
      repositoryUpdateStep.getTargetVersion(),
      repositoryId
    );
    return notRunYet(storeForRepository(repositoryId), repositoryUpdateStep);
  }

  private ConfigurationEntryStore<UpdateVersionInfo> storeForRepository(String repositoryId) {
    return storeFactory.withType(UpdateVersionInfo.class).withName(STORE_NAME).forRepository(repositoryId).build();
  }

  private boolean notRunYet(ConfigurationEntryStore<UpdateVersionInfo> store, UpdateStepTarget updateStep) {
    UpdateVersionInfo updateVersionInfo = store.get(updateStep.getAffectedDataType());
    if (updateVersionInfo == null) {
      LOG.trace("no updates for type {} run yet; step will be executed", updateStep.getAffectedDataType());
      return true;
    }
    boolean result = updateStep.getTargetVersion().isNewer(updateVersionInfo.getLatestVersion());
    LOG.trace("latest version for type {}: {}; step will be executed: {}",
      updateStep.getAffectedDataType(),
      updateVersionInfo.getLatestVersion(),
      result
    );
    return result;
  }

  private abstract class UpdateStepWrapper implements UpdateStepTarget {
    private final UpdateStepTarget delegateTarget;
    private final boolean coreUpdate;

    protected UpdateStepWrapper(UpdateStepTarget delegateTarget) {
      this.delegateTarget = delegateTarget;
      this.coreUpdate = delegateTarget instanceof CoreUpdateStep;
    }

    @Override
    public Version getTargetVersion() {
      return delegateTarget.getTargetVersion();
    }

    @Override
    public String getAffectedDataType() {
      return delegateTarget.getAffectedDataType();
    }

    public boolean isCoreUpdate() {
      return coreUpdate;
    }

    public boolean isGlobalUpdateStep() {
      return true;
    }

    public UpdateStepTarget getDelegateTarget() {
      return delegateTarget;
    }

    @SuppressWarnings("java:S112") // we explicitly want to allow all kinds of exceptions here
    abstract void doUpdate() throws Exception;

    void doUpdate(String repositoryId) throws Exception {}
  }

  private class RepositoryUpdateStepWrapper extends UpdateStepWrapper {
    public RepositoryUpdateStepWrapper(RepositoryUpdateStep delegateTarget) {
      super(delegateTarget);
    }

    @Override
    public boolean isGlobalUpdateStep() {
      return false;
    }

    @Override
    public RepositoryUpdateStep getDelegateTarget() {
      return (RepositoryUpdateStep) super.getDelegateTarget();
    }

    @Override
    void doUpdate() {
      repositoryUpdateIterator.forEachRepositoryWithException(this::doUpdate);
    }

    @Override
    void doUpdate(String repositoryId) throws Exception {
      doUpdateForRepository(repositoryId, getDelegateTarget());
    }
  }
}
