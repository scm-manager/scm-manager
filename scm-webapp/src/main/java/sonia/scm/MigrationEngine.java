package sonia.scm;

import sonia.scm.migration.UpdateStep;

import javax.inject.Inject;
import java.util.Set;

public class MigrationEngine {

  private final Set<UpdateStep> steps;

  @Inject
  public MigrationEngine(Set<UpdateStep> steps) {
    this.steps = steps;
  }

  public void migrate() {
    steps.forEach(UpdateStep::doUpdate);
  }
}
