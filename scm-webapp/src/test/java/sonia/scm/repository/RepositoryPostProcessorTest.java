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
