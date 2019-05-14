package sonia.scm.update;

import sonia.scm.migration.UpdateStep;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class UpdateEngine {

  private static final String STORE_NAME = "executedUpdates";

  private final List<UpdateStep> steps;
  private final ConfigurationEntryStore<UpdateVersionInfo> store;

  @Inject
  public UpdateEngine(Set<UpdateStep> steps, ConfigurationEntryStoreFactory storeFactory) {
    this.steps = sortSteps(steps);
    this.store = storeFactory.withType(UpdateVersionInfo.class).withName(STORE_NAME).build();
  }

  private List<UpdateStep> sortSteps(Set<UpdateStep> steps) {
    return steps.stream()
      .sorted(Comparator.comparing(UpdateStep::getTargetVersion).reversed())
      .collect(toList());
  }

  public void update() {
    steps
      .stream()
      .filter(this::notRunYet)
      .forEach(this::execute);
  }

  private void execute(UpdateStep updateStep) {
    updateStep.doUpdate();
    UpdateVersionInfo newVersionInfo = new UpdateVersionInfo(updateStep.getTargetVersion().getParsedVersion());
    store.put(updateStep.affectedDataType(), newVersionInfo);
  }

  private boolean notRunYet(UpdateStep updateStep) {
    UpdateVersionInfo updateVersionInfo = store.get(updateStep.affectedDataType());
    if (updateVersionInfo == null) {
      return true;
    }
    return updateStep.getTargetVersion().isNewer(updateVersionInfo.getLatestVersion());
  }
}
