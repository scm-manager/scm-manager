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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import sonia.scm.HandlerEventType;
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.version.Version;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RepositoryUpdateAfterCreationHookTest {

  private static final Repository REPOSITORY = new Repository("1", "git", "space", "X");

  @Test
  @SuppressWarnings("java:S6073") // this is a false positive (sonar seems not to detect the 'argThat' in the method)
  void shouldSetHighestVersion() {
    UpdateStepStore updateStepStore = mock(UpdateStepStore.class);

    RepositoryUpdateAfterCreationHook hook = new RepositoryUpdateAfterCreationHook(
      ImmutableSet.of(
        createStep("1.1.0", "test.data"),
        createStep("1.0.0", "test.data"),
        createStep("2.0.0", "more.data"),
        createStep("3.0.0", "more.data")
      ),
      updateStepStore);

    hook.updateRepository(new RepositoryEvent(HandlerEventType.CREATE, REPOSITORY));

    verify(updateStepStore).storeExecutedUpdate(eq("1"), updateStepWith("1.1.0", "test.data"));
    verify(updateStepStore).storeExecutedUpdate(eq("1"), updateStepWith("3.0", "more.data"));
  }

  private RepositoryUpdateStep updateStepWith(String version, String dataType) {
    return argThat(
      argument -> argument.getTargetVersion().equals(Version.parse(version))
        && argument.getAffectedDataType().equals(dataType)
    );
  }

  private RepositoryUpdateStep createStep(String versionString, String dataType) {
    return new RepositoryUpdateStep() {
      @Override
      public void doUpdate(RepositoryUpdateContext repositoryUpdateContext) {
      }

      @Override
      public Version getTargetVersion() {
        return Version.parse(versionString);
      }

      @Override
      public String getAffectedDataType() {
        return dataType;
      }

      @Override
      public String toString() {
        return String.format("Test update step with type %s for version %s", dataType, versionString);
      }
    };
  }
}
