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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.GitRepositoryConfigChangedEvent;
import sonia.scm.event.ScmEventBus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DefaultBranchChangedDispatcherTest {

  @Mock
  private ScmEventBus eventBus;

  @InjectMocks
  private DefaultBranchChangedDispatcher dispatcher;

  @Captor
  private ArgumentCaptor<Object> eventCaptor;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @Test
  void shouldFireDefaultBranchChangedEvent() {
    dispatcher.handleConfigUpdate(changedEvent("master", "main"));

    verify(eventBus).post(eventCaptor.capture());
    assertThat(eventCaptor.getValue())
      .isInstanceOfSatisfying(DefaultBranchChangedEvent.class, event -> {
        assertThat(event.getRepository()).isSameAs(repository);
        assertThat(event.getOldDefaultBranch()).isEqualTo("master");
        assertThat(event.getNewDefaultBranch()).isEqualTo("main");
      });
  }

  @Test
  void shouldNotFireEventIfDefaultBranchHasNotChanged() {
    dispatcher.handleConfigUpdate(changedEvent("develop", "develop"));

    verifyNoInteractions(eventBus);
  }

  private GitRepositoryConfigChangedEvent changedEvent(String oldDefaultBranch, String newDefaultBranch) {
    return new GitRepositoryConfigChangedEvent(
      repository, config(oldDefaultBranch), config(newDefaultBranch)
    );
  }

  private GitRepositoryConfig config(String defaultBranch) {
    GitRepositoryConfig config = new GitRepositoryConfig();
    config.setDefaultBranch(defaultBranch);
    return config;
  }

}
