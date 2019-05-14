package sonia.scm;

import sonia.scm.migration.UpdateStep;

import javax.inject.Inject;
import java.util.Set;

public class UpdateEngine {

  private final Set<UpdateStep> steps;

  @Inject
  public UpdateEngine(Set<UpdateStep> steps) {
    this.steps = steps;
  }

  public void update() {
    steps.forEach(UpdateStep::doUpdate);
  }
}
