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

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.NamespaceUpdateContext;
import sonia.scm.migration.NamespaceUpdateStep;
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.migration.UpdateException;
import sonia.scm.migration.UpdateStep;
import sonia.scm.migration.UpdateStepTarget;
import sonia.scm.version.Version;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

public class UpdateEngine {

  public static final Logger LOG = LoggerFactory.getLogger(UpdateEngine.class);

  private final List<UpdateStepWrapper> steps;
  private final RepositoryUpdateIterator repositoryUpdateIterator;
  private final NamespaceUpdateIterator namespaceUpdateIterator;
  private final UpdateStepStore updateStepStore;

  @Inject
  public UpdateEngine(
    Set<UpdateStep> globalSteps,
    Set<RepositoryUpdateStep> repositorySteps,
    Set<NamespaceUpdateStep> namespaceSteps,
    RepositoryUpdateIterator repositoryUpdateIterator,
    NamespaceUpdateIterator namespaceUpdateIterator, UpdateStepStore updateStepStore) {
    this.repositoryUpdateIterator = repositoryUpdateIterator;
    this.namespaceUpdateIterator = namespaceUpdateIterator;
    this.updateStepStore = updateStepStore;
    this.steps = sortSteps(globalSteps, repositorySteps, namespaceSteps);
  }

  private List<UpdateStepWrapper> sortSteps(Set<UpdateStep> globalSteps, Set<RepositoryUpdateStep> repositorySteps, Set<NamespaceUpdateStep> namespaceSteps) {
    LOG.trace("sorting available update steps:");
    List<UpdateStepWrapper> sortedSteps =
      concat(
        concat(
          globalSteps.stream().filter(this::notRunYet).map(GlobalUpdateStepWrapper::new),
          repositorySteps.stream().map(RepositoryUpdateStepWrapper::new)),
        namespaceSteps.stream().map(NamespaceUpdateStepWrapper::new)
      )
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

  private static boolean isCoreUpdateStep(UpdateStepWrapper updateStep) {
    return updateStep.isCoreUpdate();
  }

  public void update() {
    steps.forEach(this::execute);
  }

  public void update(String repositoryId) {
    steps.forEach(step -> execute(step, repositoryId));
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

  private boolean notRunYet(UpdateStep updateStep) {
    LOG.trace("checking whether to run update step for type {} and version {}",
      updateStep.getAffectedDataType(),
      updateStep.getTargetVersion()
    );
    return updateStepStore.notRunYet(updateStep);
  }

  private abstract static class UpdateStepWrapper implements UpdateStepTarget {

    private final UpdateStepTarget delegate;

    protected UpdateStepWrapper(UpdateStepTarget delegate) {
      this.delegate = delegate;
    }

    @Override
    public Version getTargetVersion() {
      return delegate.getTargetVersion();
    }

    @Override
    public String getAffectedDataType() {
      return delegate.getAffectedDataType();
    }

    abstract boolean isGlobalUpdateStep();

    abstract boolean isCoreUpdate();

    @SuppressWarnings("java:S112") // we explicitly want to allow all kinds of exceptions here
    abstract void doUpdate() throws Exception;

    @SuppressWarnings("java:S112") // we explicitly want to allow all kinds of exceptions here
    abstract void doUpdate(String repositoryId) throws Exception;
  }

  private class GlobalUpdateStepWrapper extends UpdateStepWrapper {
    private final UpdateStep delegate;
    private final boolean coreUpdate;

    protected GlobalUpdateStepWrapper(UpdateStep delegate) {
      super(delegate);
      this.delegate = delegate;
      this.coreUpdate = delegate instanceof CoreUpdateStep;
    }

    @Override
    public Version getTargetVersion() {
      return delegate.getTargetVersion();
    }

    @Override
    public String getAffectedDataType() {
      return delegate.getAffectedDataType();
    }

    public boolean isCoreUpdate() {
      return coreUpdate;
    }

    public boolean isGlobalUpdateStep() {
      return true;
    }

    void doUpdate() throws Exception {
      LOG.info("running update step for type {} and version {} (class {})",
        delegate.getAffectedDataType(),
        delegate.getTargetVersion(),
        delegate.getClass().getName()
      );
      delegate.doUpdate();
      updateStepStore.storeExecutedUpdate(delegate);
    }

    void doUpdate(String repositoryId) {
      // nothing to do for repositories here
    }
  }

  private class RepositoryUpdateStepWrapper extends UpdateStepWrapper {

    private final RepositoryUpdateStep delegate;

    public RepositoryUpdateStepWrapper(RepositoryUpdateStep delegate) {
      super(delegate);
      this.delegate = delegate;
    }

    @Override
    public boolean isGlobalUpdateStep() {
      return false;
    }

    @Override
    boolean isCoreUpdate() {
      return false;
    }

    @Override
    void doUpdate() {
      repositoryUpdateIterator.updateEachRepository(this::doUpdate);
    }

    @Override
    void doUpdate(String repositoryId) throws Exception {
      if (notRunYet(repositoryId)) {
        LOG.info("running update step for type {} and version {} (class {}) for repository id {}",
          delegate.getAffectedDataType(),
          delegate.getTargetVersion(),
          delegate.getClass().getName(),
          repositoryId
        );
        delegate.doUpdate(new RepositoryUpdateContext(repositoryId));
        updateStepStore.storeExecutedUpdate(repositoryId, delegate);
      }
    }

    private boolean notRunYet(String repositoryId) {
      LOG.trace("checking whether to run update step for type {} and version {} on repository id {}",
        delegate.getAffectedDataType(),
        delegate.getTargetVersion(),
        repositoryId
      );
      return updateStepStore.notRunYet(repositoryId, delegate);
    }
  }

  private class NamespaceUpdateStepWrapper extends UpdateStepWrapper {

    private final NamespaceUpdateStep delegate;

    public NamespaceUpdateStepWrapper(NamespaceUpdateStep delegate) {
      super(delegate);
      this.delegate = delegate;
    }

    @Override
    public boolean isGlobalUpdateStep() {
      return false;
    }

    @Override
    boolean isCoreUpdate() {
      return false;
    }

    @Override
    void doUpdate() {
      namespaceUpdateIterator.updateEachNamespace(this::doUpdate);
    }

    @Override
    void doUpdate(String namespace) throws Exception {
      if (notRunYet(namespace)) {
        LOG.info("running update step for type {} and version {} (class {}) for namespace {}",
          delegate.getAffectedDataType(),
          delegate.getTargetVersion(),
          delegate.getClass().getName(),
          namespace
        );
        delegate.doUpdate(new NamespaceUpdateContext(namespace));
        updateStepStore.storeExecutedUpdate(namespace, delegate);
      }
    }

    private boolean notRunYet(String namespace) {
      LOG.trace("checking whether to run update step for type {} and version {} on namespace {}",
        delegate.getAffectedDataType(),
        delegate.getTargetVersion(),
        namespace
      );
      return updateStepStore.notRunYet(namespace, delegate);
    }
  }
}
