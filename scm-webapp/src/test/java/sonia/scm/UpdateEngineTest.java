package sonia.scm;

import org.junit.jupiter.api.Test;
import sonia.scm.migration.UpdateStep;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateEngineTest {

  List<String> processedUpdates = new ArrayList<>();

  @Test
  void shouldProcessStepsInCorrectOrder() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("1.1.1"));
    updateSteps.add(new FixedVersionUpdateStep("1.2.0"));
    updateSteps.add(new FixedVersionUpdateStep("1.1.0"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps);
    updateEngine.update();

    assertThat(processedUpdates)
      .containsExactly("1.1.0", "1.1.1", "1.2.0");
  }

  class FixedVersionUpdateStep implements UpdateStep {
    private final String version;

    FixedVersionUpdateStep(String version) {
      this.version = version;
    }

    @Override
    public String getTargetVersion() {
      return version;
    }

    @Override
    public void doUpdate() {
      processedUpdates.add(version);
    }
  }
}
