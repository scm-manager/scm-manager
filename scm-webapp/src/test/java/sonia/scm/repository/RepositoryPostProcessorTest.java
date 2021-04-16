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

package sonia.scm.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RepositoryPostProcessorTest {

  @Mock
  private ScmEventBus eventBus;

  @InjectMocks
  RepositoryPostProcessor repositoryPostProcessor;

  @Test
  void shouldSetHealthChecksForRepository() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    repositoryPostProcessor.setCheckResults(repository.clone(), singleton(new HealthCheckFailure("HOG", "improbable", "This is not very probable")));

    repositoryPostProcessor.postProcess(repository);

    assertThat(repository.getHealthCheckFailures())
      .extracting("id")
      .containsExactly("HOG");
  }

  @Test
  void shouldSetEmptyListOfHealthChecksWhenNoResultsExist() {
    Repository repository = RepositoryTestData.createHeartOfGold();

    repositoryPostProcessor.postProcess(repository);

    assertThat(repository.getHealthCheckFailures())
      .isNotNull()
      .isEmpty();
  }

  @Test
  void shouldSetHealthChecksForRepositoryInSetter() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    repositoryPostProcessor.setCheckResults(repository, singleton(new HealthCheckFailure("HOG", "improbable", "This is not very probable")));

    assertThat(repository.getHealthCheckFailures())
      .extracting("id")
      .containsExactly("HOG");
  }

  @Test
  void shouldTriggerHealthCheckEventForNewFailure() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    repositoryPostProcessor.setCheckResults(repository, singleton(new HealthCheckFailure("HOG", "improbable", "This is not very probable")));

    verify(eventBus).post(argThat(event -> {
      HealthCheckEvent healthCheckEvent = (HealthCheckEvent) event;
      assertThat(healthCheckEvent.getRepository())
        .isEqualTo(repository);
      assertThat(healthCheckEvent.getPreviousFailures())
        .isEmpty();
      assertThat(((HealthCheckEvent) event).getCurrentFailures())
        .extracting("id")
        .containsExactly("HOG");
      return true;
    }));
  }

  @Test
  void shouldTriggerHealthCheckEventForDifferentFailure() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    repositoryPostProcessor.setCheckResults(repository, singleton(new HealthCheckFailure("HOG", "improbable", "This is not very probable")));
    repositoryPostProcessor.setCheckResults(repository, singleton(new HealthCheckFailure("VOG", "vogons", "Erased by Vogons")));

    verify(eventBus).post(argThat(event -> {
      HealthCheckEvent healthCheckEvent = (HealthCheckEvent) event;
      if (healthCheckEvent.getPreviousFailures().isEmpty()) {
        return false; // ignore event from first checks
      }
      assertThat((healthCheckEvent).getRepository())
        .isEqualTo(repository);
      assertThat((healthCheckEvent).getPreviousFailures())
        .extracting("id")
        .containsExactly("HOG");
      assertThat((healthCheckEvent).getCurrentFailures())
        .extracting("id")
        .containsExactly("VOG");
      return true;
    }));
  }
}
