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
