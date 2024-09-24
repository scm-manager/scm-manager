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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.api.BranchesCommandBuilder;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.store.ConfigurationStore;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
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

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private BranchesCommandBuilder branchesCommand;
  @Mock
  private Branches branches;

  @InjectMocks
  private GitRepositoryConfigInitializer initializer;

  @Test
  void shouldSkipNonGitRepositories() throws IOException {
    REPOSITORY.setType("svn");

    initializer.initConfig(event);

    verify(event, never()).getContext();
  }

  @BeforeEach
  void initEvent() {
    when(event.getRepository()).thenReturn(REPOSITORY);
  }

  @Nested
  class ForGitRepositories {

    @BeforeEach
    void initMocks() throws IOException {
      when(storeProvider.get(REPOSITORY)).thenReturn(configStore);
      REPOSITORY.setType("git");
      lenient().when(serviceFactory.create(event.getRepository())).thenReturn(service);
      lenient().when(service.getBranchesCommand()).thenReturn(branchesCommand);
      lenient().when(branchesCommand.getBranches()).thenReturn(branches);
    }

    @Test
    void shouldDoNothingIfDefaultBranchAlreadySet() throws IOException {
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
        lenient().when(repoHandler.getConfig()).thenReturn(gitConfig);
      }

      @Test
      void shouldSetDefaultBranchFromPushIfNoGitConfigYet() throws IOException {
        when(configStore.get()).thenReturn(null);
        initEvent(List.of("main"));

        initializer.initConfig(event);

        verify(event).getContext();
      }

      @Test
      void shouldDetermineAndApplyDefaultBranch_GlobalDefault() throws IOException {
        initEvent(List.of("global_default", "main", "master"));
        when(configStore.get()).thenReturn(new GitRepositoryConfig(null));

        initializer.initConfig(event);

        verify(configStore).set(argThat(arg -> {
          assertThat(arg.getDefaultBranch()).isEqualTo("global_default");
          return true;
        }));
      }

      @Test
      void shouldDetermineAndApplyDefaultBranch_Main() throws IOException {
        initEvent(List.of("master", "main"));
        when(configStore.get()).thenReturn(new GitRepositoryConfig(null));

        initializer.initConfig(event);

        verify(configStore).set(argThat(arg -> {
          assertThat(arg.getDefaultBranch()).isEqualTo("main");
          return true;
        }));
      }

      @Test
      void shouldDetermineAndApplyDefaultBranch_Master() throws IOException {
        initEvent(List.of("develop", "master"));
        when(configStore.get()).thenReturn(new GitRepositoryConfig(null));

        initializer.initConfig(event);

        verify(configStore).set(argThat(arg -> {
          assertThat(arg.getDefaultBranch()).isEqualTo("master");
          return true;
        }));
      }

      @Test
      void shouldDetermineAndApplyDefaultBranchFromRepository_Staging() throws IOException {
        when(configStore.get()).thenReturn(null);
        initEvent(List.of("master", "main"));
        when(branches.getBranches()).thenReturn(List.of(new Branch("staging", "abc", true)));

        initializer.initConfig(event);

        verify(configStore).set(argThat(arg -> {
          assertThat(arg.getDefaultBranch()).isEqualTo("staging");
          return true;
        }));
      }

      @Test
      void shouldDetermineAndApplyDefaultBranch_BestMatch() throws IOException {
        initEvent(List.of("test/123", "trillian", "dent"));
        when(configStore.get()).thenReturn(new GitRepositoryConfig(null));

        initializer.initConfig(event);

        verify(configStore).set(argThat(arg -> {
          assertThat(arg.getDefaultBranch()).isEqualTo("dent");
          return true;
        }));
      }

      @Test
      void shouldDetermineAndApplyDefaultBranch_AnyFallback() throws IOException {
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
      lenient().when(event.getContext()).thenReturn(hookContext);
      HookBranchProvider branchProvider = mock(HookBranchProvider.class);
      lenient().when(hookContext.getBranchProvider()).thenReturn(branchProvider);
      lenient().when(branchProvider.getCreatedOrModified()).thenReturn(branches);
    }
  }
}
