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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.migration.UpdateStep;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;
import sonia.scm.version.Version;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static sonia.scm.version.Version.parse;

class UpdateEngineTest {

  ConfigurationEntryStoreFactory storeFactory = new InMemoryConfigurationEntryStoreFactory();
  RepositoryUpdateIterator repositoryUpdateIterator = mock(RepositoryUpdateIterator.class, CALLS_REAL_METHODS);

  List<String> processedUpdates = new ArrayList<>();

  @BeforeEach
  void mockRepositories() {
    doAnswer(invocation -> {
      Consumer<String> consumer = invocation.getArgument(0, Consumer.class);
      consumer.accept("42");
      consumer.accept("1337");
      return null;
    }).when(repositoryUpdateIterator).forEachRepository(any());
  }

  @Test
  void shouldProcessStepsInCorrectOrder() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));
    updateSteps.add(new FixedVersionUpdateStep("test", "1.2.0"));
    updateSteps.add(new FixedVersionUpdateStep("test", "1.1.0"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, emptySet(), storeFactory, repositoryUpdateIterator);
    updateEngine.update();

    assertThat(processedUpdates)
      .containsExactly("test:1.1.0", "test:1.1.1", "test:1.2.0");
  }

  @Test
  void shouldProcessStepsInCorrectOrderWithRepositoryUpdates() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();
    LinkedHashSet<RepositoryUpdateStep> repositoryUpdateSteps = new LinkedHashSet<>();

    repositoryUpdateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));
    updateSteps.add(new FixedVersionUpdateStep("test", "1.2.0"));
    repositoryUpdateSteps.add(new FixedVersionUpdateStep("test", "1.1.0"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, repositoryUpdateSteps, storeFactory, repositoryUpdateIterator);
    updateEngine.update();

    assertThat(processedUpdates)
      .containsExactly("test:1.1.0-42", "test:1.1.0-1337", "test:1.1.1-42", "test:1.1.1-1337", "test:1.2.0");
  }

  @Test
  void shouldProcessStepsForSingleRepository() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();
    LinkedHashSet<RepositoryUpdateStep> repositoryUpdateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.2.0"));
    repositoryUpdateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));
    repositoryUpdateSteps.add(new FixedVersionUpdateStep("test", "1.1.0"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, repositoryUpdateSteps, storeFactory, repositoryUpdateIterator);
    updateEngine.update("1337");

    assertThat(processedUpdates)
      .containsExactly("test:1.1.0-1337", "test:1.1.1-1337");
  }

  @Test
  void shouldProcessCoreStepsBeforeOther() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.2.0"));
    updateSteps.add(new CoreFixedVersionUpdateStep("core", "1.2.0"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, emptySet(), storeFactory, repositoryUpdateIterator);
    updateEngine.update();

    assertThat(processedUpdates)
      .containsExactly("core:1.2.0", "test:1.2.0");
  }

  @Test
  void shouldProcessGlobalStepsBeforeRepository() {
    Set<UpdateStep> updateSteps = singleton(new FixedVersionUpdateStep("test", "1.2.0"));
    Set<RepositoryUpdateStep> repositoryUpdateSteps = singleton(new FixedVersionUpdateStep("test", "1.2.0"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, repositoryUpdateSteps, storeFactory, repositoryUpdateIterator);
    updateEngine.update();

    assertThat(processedUpdates)
      .containsExactly("test:1.2.0", "test:1.2.0-42", "test:1.2.0-1337");
  }

  @Test
  void shouldRunGlobalStepsOnlyOnce() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));

    UpdateEngine firstUpdateEngine = new UpdateEngine(updateSteps, emptySet(), storeFactory, repositoryUpdateIterator);
    firstUpdateEngine.update();

    processedUpdates.clear();

    UpdateEngine secondUpdateEngine = new UpdateEngine(updateSteps, emptySet(), storeFactory, repositoryUpdateIterator);
    secondUpdateEngine.update();

    assertThat(processedUpdates).isEmpty();
  }

  @Test
  void shouldRunRepositoryStepsOnlyOnce() {
    LinkedHashSet<RepositoryUpdateStep> repositoryUpdateSteps = new LinkedHashSet<>();

    repositoryUpdateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));

    UpdateEngine firstUpdateEngine = new UpdateEngine(emptySet(), repositoryUpdateSteps, storeFactory, repositoryUpdateIterator);
    firstUpdateEngine.update();

    processedUpdates.clear();

    repositoryUpdateSteps.add(new FixedVersionUpdateStep("test", "1.2.0"));
    UpdateEngine secondUpdateEngine = new UpdateEngine(emptySet(), repositoryUpdateSteps, storeFactory, repositoryUpdateIterator);
    secondUpdateEngine.update();

    assertThat(processedUpdates)
      .containsExactly("test:1.2.0-42", "test:1.2.0-1337");
  }

  @Test
  void shouldRunStepsForDifferentTypesIndependently() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, emptySet(), storeFactory, repositoryUpdateIterator);
    updateEngine.update();

    processedUpdates.clear();

    updateSteps.add(new FixedVersionUpdateStep("other", "1.1.1"));

    updateEngine = new UpdateEngine(updateSteps, emptySet(), storeFactory, repositoryUpdateIterator);
    updateEngine.update();

    assertThat(processedUpdates).containsExactly("other:1.1.1");
  }

  class FixedVersionUpdateStep implements UpdateStep, RepositoryUpdateStep {
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
      processedUpdates.add(type + ":" + version);
    }

    @Override
    public void doUpdate(RepositoryUpdateContext repositoryUpdateContext) throws Exception {
      processedUpdates.add(type + ":" + version + "-" + repositoryUpdateContext.getRepositoryId());
    }
  }

  class CoreFixedVersionUpdateStep extends FixedVersionUpdateStep implements CoreUpdateStep {
    CoreFixedVersionUpdateStep(String type, String version) {
      super(type, version);
    }
  }
}
