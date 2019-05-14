package sonia.scm;

import sonia.scm.migration.UpdateStep;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class UpdateEngine {

  private final List<UpdateStep> steps;

  @Inject
  public UpdateEngine(Set<UpdateStep> steps) {
    this.steps = sortSteps(steps);
  }

  private List<UpdateStep> sortSteps(Set<UpdateStep> steps) {
    Comparator<UpdateStep> compareByVersion = Comparator.comparing(step -> Version.parse(step.getTargetVersion()));
    return steps.stream()
      .sorted(compareByVersion.reversed())
      .collect(toList());
  }

  public void update() {
    steps.forEach(UpdateStep::doUpdate);
  }
}
