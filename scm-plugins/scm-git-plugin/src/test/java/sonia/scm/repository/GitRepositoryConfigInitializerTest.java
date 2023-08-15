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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.store.ConfigurationStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitRepositoryConfigInitializerTest {

  private static final Repository REPOSITORY = RepositoryTestData.create42Puzzle();

  @Mock
  private GitRepositoryHandler repoHandler;

  @Mock
  private GitRepositoryConfigStoreProvider storeProvider;

  @Mock
  private ConfigurationStore<GitRepositoryConfig> configStore;

  @Mock
  private PostReceiveRepositoryHookEvent event;

  @InjectMocks
  private GitRepositoryConfigInitializer initializer;

  @BeforeEach
  void initMocks() {
    when(storeProvider.get(REPOSITORY)).thenReturn(configStore);
    when(event.getRepository()).thenReturn(REPOSITORY);
  }

  @Test
  void shouldDoNothingIfDefaultBranchAlreadySet() {
    when(configStore.get()).thenReturn(new GitRepositoryConfig("any"));

    initializer.initConfig(event);

    verify(event, never()).getContext();
  }

  @Nested
  class WithGlobalConfig {

    @BeforeEach
    void initRepoHandler() {
      GitConfig gitConfig = new GitConfig();
      gitConfig.setDefaultBranch("global_default");
      when(repoHandler.getConfig()).thenReturn(gitConfig);
    }

    @Test
    void shouldSetDefaultBranchIfNoGitConfigYet() {
      when(configStore.get()).thenReturn(null);
      initEvent(List.of("main"));

      initializer.initConfig(event);

      verify(event).getContext();
    }

    @Test
    void shouldDetermineAndApplyDefaultBranch_GlobalDefault() {
      initEvent(List.of("global_default", "main", "master"));
      when(configStore.get()).thenReturn(new GitRepositoryConfig(null));

      initializer.initConfig(event);

      verify(configStore).set(argThat(arg -> {
        assertThat(arg.getDefaultBranch()).isEqualTo("global_default");
        return true;
      }));
    }

    @Test
    void shouldDetermineAndApplyDefaultBranch_Main() {
      initEvent(List.of("master", "main"));
      when(configStore.get()).thenReturn(new GitRepositoryConfig(null));

      initializer.initConfig(event);

      verify(configStore).set(argThat(arg -> {
        assertThat(arg.getDefaultBranch()).isEqualTo("main");
        return true;
      }));
    }

    @Test
    void shouldDetermineAndApplyDefaultBranch_Master() {
      initEvent(List.of("develop", "master"));
      when(configStore.get()).thenReturn(new GitRepositoryConfig(null));

      initializer.initConfig(event);

      verify(configStore).set(argThat(arg -> {
        assertThat(arg.getDefaultBranch()).isEqualTo("master");
        return true;
      }));
    }

    @Test
    void shouldDetermineAndApplyDefaultBranch_BestMatch() {
      initEvent(List.of("test/123", "trillian", "dent"));
      when(configStore.get()).thenReturn(new GitRepositoryConfig(null));

      initializer.initConfig(event);

      verify(configStore).set(argThat(arg -> {
        assertThat(arg.getDefaultBranch()).isEqualTo("dent");
        return true;
      }));
    }

    @Test
    void shouldDetermineAndApplyDefaultBranch_AnyFallback() {
      initEvent(List.of("test/123", "x/y/z"));
      when(configStore.get()).thenReturn(new GitRepositoryConfig(null));

      initializer.initConfig(event);

      verify(configStore).set(argThat(arg -> {
        assertThat(arg.getDefaultBranch()).isEqualTo("test/123");
        return true;
      }));
    }
  }

  private void initEvent(List<String> branches) {
    HookContext hookContext = mock(HookContext.class);
    when(event.getContext()).thenReturn(hookContext);
    HookBranchProvider branchProvider = mock(HookBranchProvider.class);
    when(hookContext.getBranchProvider()).thenReturn(branchProvider);
    when(branchProvider.getCreatedOrModified()).thenReturn(branches);
  }

}
