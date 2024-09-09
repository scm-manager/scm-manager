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

package sonia.scm.repository.spi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Modified;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.ModificationsCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.RepositoryHookType.PRE_RECEIVE;

@ExtendWith(MockitoExtension.class)
class FileLockPreCommitHookTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private GitFileLockStoreFactory fileLockStoreFactory;
  @Mock
  private GitFileLockStoreFactory.GitFileLockStore fileLockStore;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;

  @InjectMocks
  private FileLockPreCommitHook hook;

  @Mock
  private HookContext context;

  @BeforeEach
  void initLockStore() {
    when(fileLockStoreFactory.create(REPOSITORY))
      .thenReturn(fileLockStore);
  }

  @Test
  void shouldIgnoreRepositoriesWithoutLockSupport() {
    PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(context, REPOSITORY, PRE_RECEIVE));

    hook.checkForLocks(event);

    verify(serviceFactory, never()).create(any(Repository.class));
  }

  @Nested
  class WithLocks {

    @Mock
    private HookChangesetBuilder changesetBuilder;
    @Mock(answer = Answers.RETURNS_SELF)
    private ModificationsCommandBuilder modificationsCommand;

    private String currentChangesetId;

    @BeforeEach
    void initService() {
      when(serviceFactory.create(REPOSITORY))
        .thenReturn(service);
      when(service.getModificationsCommand())
        .thenReturn(modificationsCommand);
      when(context.getChangesetProvider())
        .thenReturn(changesetBuilder);
    }

    @BeforeEach
    void initLocks() {
      when(fileLockStore.hasLocks()).thenReturn(true);
    }

    @BeforeEach
    void initModifications() throws IOException {
      when(modificationsCommand.revision(anyString()))
        .thenAnswer(invocation -> {
          currentChangesetId = invocation.getArgument(0, String.class);
          return modificationsCommand;
        });
      when(modificationsCommand.getModifications())
        .thenAnswer(invocation ->
          new Modifications(
            currentChangesetId,
            new Modified("path-1-" + currentChangesetId),
            new Modified("path-2-" + currentChangesetId)
          ));
    }

    @Test
    void shouldCheckAllPathsForLocks() {
      PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(context, REPOSITORY, PRE_RECEIVE));
      when(changesetBuilder.getChangesets())
        .thenReturn(asList(
          new Changeset("1", null, null),
          new Changeset("2", null, null)
        ));

      hook.checkForLocks(event);

      verify(fileLockStore).assertModifiable("path-1-1");
      verify(fileLockStore).assertModifiable("path-2-1");
      verify(fileLockStore).assertModifiable("path-1-2");
      verify(fileLockStore).assertModifiable("path-2-2");
    }
  }
}
