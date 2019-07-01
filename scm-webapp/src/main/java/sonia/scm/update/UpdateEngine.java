package sonia.scm.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.UpdateException;
import sonia.scm.migration.UpdateStep;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class UpdateEngine {

  public static final Logger LOG = LoggerFactory.getLogger(UpdateEngine.class);

  private static final String STORE_NAME = "executedUpdates";

  private final List<UpdateStep> steps;
  private final ConfigurationEntryStore<UpdateVersionInfo> store;

  @Inject
  public UpdateEngine(Set<UpdateStep> steps, ConfigurationEntryStoreFactory storeFactory) {
    this.steps = sortSteps(steps);
    this.store = storeFactory.withType(UpdateVersionInfo.class).withName(STORE_NAME).build();
  }

  private List<UpdateStep> sortSteps(Set<UpdateStep> steps) {
    LOG.trace("sorting available update steps:");
    List<UpdateStep> sortedSteps = steps.stream()
      .sorted(
        Comparator
          .comparing(UpdateStep::getTargetVersion)
          .thenComparing(this::isCoreUpdateStep)
          .reversed())
      .collect(toList());
    sortedSteps.forEach(step -> LOG.trace("{} for version {}", step.getAffectedDataType(), step.getTargetVersion()));
    return sortedSteps;
  }

  private boolean isCoreUpdateStep(UpdateStep updateStep) {
    return updateStep instanceof CoreUpdateStep;
  }

  public void update() {
    steps
      .stream()
      .filter(this::notRunYet)
      .forEach(this::execute);
  }

  private void execute(UpdateStep updateStep) {
    try {
      LOG.info("running update step for type {} and version {}",
        updateStep.getAffectedDataType(),
        updateStep.getTargetVersion()
      );
      updateStep.doUpdate();
    } catch (Exception e) {
      throw new UpdateException(
        String.format(
          "could not execute update for type %s to version %s in %s",
          updateStep.getAffectedDataType(),
          updateStep.getTargetVersion(),
          updateStep.getClass()),
        e);
    }
    UpdateVersionInfo newVersionInfo = new UpdateVersionInfo(updateStep.getTargetVersion().getParsedVersion());
    store.put(updateStep.getAffectedDataType(), newVersionInfo);
  }

  private boolean notRunYet(UpdateStep updateStep) {
    LOG.trace("checking whether to run update step for type {} and version {}",
      updateStep.getAffectedDataType(),
      updateStep.getTargetVersion()
    );
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
}
