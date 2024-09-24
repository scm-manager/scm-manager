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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.migration.NamespaceUpdateContext;
import sonia.scm.migration.NamespaceUpdateStep;
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.migration.UpdateStep;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.InMemoryByteConfigurationEntryStoreFactory;
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

  ConfigurationEntryStoreFactory storeFactory = new InMemoryByteConfigurationEntryStoreFactory();
  RepositoryUpdateIterator repositoryUpdateIterator = mock(RepositoryUpdateIterator.class, CALLS_REAL_METHODS);
  NamespaceUpdateIterator namespaceUpdateIterator = mock(NamespaceUpdateIterator.class, CALLS_REAL_METHODS);
  UpdateStepStore updateStepStore = new UpdateStepStore(storeFactory);

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

  @BeforeEach
  void mockNamespaces() {
    doAnswer(invocation -> {
      Consumer<String> consumer = invocation.getArgument(0, Consumer.class);
      consumer.accept("hitchhikers");
      consumer.accept("vogons");
      return null;
    }).when(namespaceUpdateIterator).forEachNamespace(any());
  }

  @Test
  void shouldProcessStepsInCorrectOrder() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));
    updateSteps.add(new FixedVersionUpdateStep("test", "1.2.0"));
    updateSteps.add(new FixedVersionUpdateStep("test", "1.1.0"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, emptySet(), emptySet(), repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
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

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, repositoryUpdateSteps, emptySet(), repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
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

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, repositoryUpdateSteps, emptySet(), repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
    updateEngine.update("1337");

    assertThat(processedUpdates)
      .containsExactly("test:1.1.0-1337", "test:1.1.1-1337");
  }

  @Test
  void shouldProcessStepsForSingleNamespace() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();
    LinkedHashSet<NamespaceUpdateStep> namespaceUpdateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.2.0"));
    namespaceUpdateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));
    namespaceUpdateSteps.add(new FixedVersionUpdateStep("test", "1.1.0"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, emptySet(), namespaceUpdateSteps, repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
    updateEngine.update("hitchhikers");

    assertThat(processedUpdates)
      .containsExactly("test:1.1.0/hitchhikers", "test:1.1.1/hitchhikers");
  }

  @Test
  void shouldProcessCoreStepsBeforeOther() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.2.0"));
    updateSteps.add(new CoreFixedVersionUpdateStep("core", "1.2.0"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, emptySet(), emptySet(), repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
    updateEngine.update();

    assertThat(processedUpdates)
      .containsExactly("core:1.2.0", "test:1.2.0");
  }

  @Test
  void shouldProcessGlobalStepsBeforeRepository() {
    Set<UpdateStep> updateSteps = singleton(new FixedVersionUpdateStep("test", "1.2.0"));
    Set<RepositoryUpdateStep> repositoryUpdateSteps = singleton(new FixedVersionUpdateStep("test", "1.2.0"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, repositoryUpdateSteps, emptySet(), repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
    updateEngine.update();

    assertThat(processedUpdates)
      .containsExactly("test:1.2.0", "test:1.2.0-42", "test:1.2.0-1337");
  }

  @Test
  void shouldRunGlobalStepsOnlyOnce() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));

    UpdateEngine firstUpdateEngine = new UpdateEngine(updateSteps, emptySet(), emptySet(), repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
    firstUpdateEngine.update();

    processedUpdates.clear();

    UpdateEngine secondUpdateEngine = new UpdateEngine(updateSteps, emptySet(), emptySet(), repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
    secondUpdateEngine.update();

    assertThat(processedUpdates).isEmpty();
  }

  @Test
  void shouldRunRepositoryStepsOnlyOnce() {
    LinkedHashSet<RepositoryUpdateStep> repositoryUpdateSteps = new LinkedHashSet<>();

    repositoryUpdateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));

    UpdateEngine firstUpdateEngine = new UpdateEngine(emptySet(), repositoryUpdateSteps, emptySet(), repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
    firstUpdateEngine.update();

    processedUpdates.clear();

    repositoryUpdateSteps.add(new FixedVersionUpdateStep("test", "1.2.0"));
    UpdateEngine secondUpdateEngine = new UpdateEngine(emptySet(), repositoryUpdateSteps, emptySet(), repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
    secondUpdateEngine.update();

    assertThat(processedUpdates)
      .containsExactly("test:1.2.0-42", "test:1.2.0-1337");
  }

  @Test
  void shouldRunNamespaceStepsOnlyOnce() {
    LinkedHashSet<NamespaceUpdateStep> namespaceUpdateSteps = new LinkedHashSet<>();

    namespaceUpdateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));

    UpdateEngine firstUpdateEngine = new UpdateEngine(emptySet(), emptySet(), namespaceUpdateSteps, repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
    firstUpdateEngine.update();

    processedUpdates.clear();

    namespaceUpdateSteps.add(new FixedVersionUpdateStep("test", "1.2.0"));
    UpdateEngine secondUpdateEngine = new UpdateEngine(emptySet(), emptySet(), namespaceUpdateSteps, repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
    secondUpdateEngine.update();

    assertThat(processedUpdates)
      .containsExactly("test:1.2.0/hitchhikers", "test:1.2.0/vogons");
  }

  @Test
  void shouldRunStepsForDifferentTypesIndependently() {
    LinkedHashSet<UpdateStep> updateSteps = new LinkedHashSet<>();

    updateSteps.add(new FixedVersionUpdateStep("test", "1.1.1"));

    UpdateEngine updateEngine = new UpdateEngine(updateSteps, emptySet(), emptySet(), repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
    updateEngine.update();

    processedUpdates.clear();

    updateSteps.add(new FixedVersionUpdateStep("other", "1.1.1"));

    updateEngine = new UpdateEngine(updateSteps, emptySet(), emptySet(), repositoryUpdateIterator, namespaceUpdateIterator, updateStepStore);
    updateEngine.update();

    assertThat(processedUpdates).containsExactly("other:1.1.1");
  }

  class FixedVersionUpdateStep implements UpdateStep, RepositoryUpdateStep, NamespaceUpdateStep {
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

    @Override
    public void doUpdate(NamespaceUpdateContext namespaceUpdateContext) throws Exception {
      processedUpdates.add(type + ":" + version + "/" + namespaceUpdateContext.getNamespace());
    }
  }

  class CoreFixedVersionUpdateStep extends FixedVersionUpdateStep implements CoreUpdateStep {
    CoreFixedVersionUpdateStep(String type, String version) {
      super(type, version);
    }
  }
}
