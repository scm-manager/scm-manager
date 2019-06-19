package sonia.scm.update;

import org.junit.jupiter.api.Test;
import sonia.scm.migration.UpdateStep;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.InMemoryConfigurationEntryStore;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;
import sonia.scm.version.Version;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.version.Version.parse;

class UpdateEngineTest {

  ConfigurationEntryStoreFactory storeFactory = new InMemoryConfigurationEntryStoreFactory();

  List<String> processedUpdates = new ArrayList<>();

  @Test
  void shouldProcessStepsInCorrectOrder() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));
    updateSteps.add(new FixedVersionUpdateStep("test", "1.2.0"));
    updateSteps.add(new FixedVersionUpdateStep("test", "1.1.0"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, storeFactory);
    updateEngine.update();

    assertThat(processedUpdates)
      .containsExactly("1.1.0", "1.1.1", "1.2.0");
  }

  @Test
  void shouldRunStepsOnlyOnce() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, storeFactory);
    updateEngine.update();

    processedUpdates.clear();

    updateEngine.update();

    assertThat(processedUpdates).isEmpty();
  }

  @Test
  void shouldRunStepsForDifferentTypesIndependently() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, storeFactory);
    updateEngine.update();

    processedUpdates.clear();

    updateSteps.add(new FixedVersionUpdateStep("other", "1.1.1"));

    updateEngine = new UpdateEngine(updateSteps, storeFactory);
    updateEngine.update();

    assertThat(processedUpdates).containsExactly("1.1.1");
  }

  class FixedVersionUpdateStep implements UpdateStep {
    private final String type;
    private final String version;

    FixedVersionUpdateStep(String type, String version) {
      this.type = type;
      this.version = version;
    }

    @Override
    public Version getTargetVersion() {
      return parse(version);
    }

    @Override
    public String getAffectedDataType() {
      return type;
    }

    @Override
    public void doUpdate() {
      processedUpdates.add(version);
    }
  }
}
