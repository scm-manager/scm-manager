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
