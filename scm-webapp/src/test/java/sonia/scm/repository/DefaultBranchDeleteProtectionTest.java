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
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.api.BranchesCommandBuilder;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.CannotDeleteDefaultBranchException;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.RepositoryHookType.PRE_RECEIVE;

@ExtendWith(MockitoExtension.class)
class DefaultBranchDeleteProtectionTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private HookContext context;
  @Mock
  private HookBranchProvider branchProvider;

  @InjectMocks
  private DefaultBranchDeleteProtection defaultBranchDeleteProtection;

  @Test
  void shouldDoNothingWithoutBranchProvider() {
    when(context.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(false);

    PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(context, REPOSITORY, PRE_RECEIVE));
    defaultBranchDeleteProtection.protectDefaultBranch(event);

    verify(context, never()).getBranchProvider();
  }

  @Test
  void shouldDoNothingWithoutDeletedBranch() {
    mockDeletedBranches(emptyList());

    PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(context, REPOSITORY, PRE_RECEIVE));
    defaultBranchDeleteProtection.protectDefaultBranch(event);

    verify(serviceFactory, never()).create(any(Repository.class));
  }

  @Nested
  class WithService {

    @Mock
    private RepositoryService service;
    @Mock(answer = Answers.RETURNS_SELF)
    private BranchesCommandBuilder branchesCommand;

    @BeforeEach
    void initRepositoryService() {
      when(serviceFactory.create(REPOSITORY)).thenReturn(service);
      when(service.getBranchesCommand()).thenReturn(branchesCommand);
    }

    @Test
    @SuppressWarnings("java:S2699")
      // we just need to make sure not exception is thrown
    void shouldDoNothingWithoutDefaultBranch() throws IOException {
      mockDeletedBranches(singletonList("anything"));
      mockExistingBranches(branch("there"), branch("is"), branch("no"), branch("default"));

      PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(context, REPOSITORY, PRE_RECEIVE));
      defaultBranchDeleteProtection.protectDefaultBranch(event);
    }

    @Test
    void shouldDoNothingIfDefaultBranchIsNotDeleted() throws IOException {
      mockDeletedBranches(singletonList("anything"));
      mockExistingBranches(branch("anything"), defaultBranch());

      PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(context, REPOSITORY, PRE_RECEIVE));
      defaultBranchDeleteProtection.protectDefaultBranch(event);

      verify(branchesCommand).setDisableCache(true);
    }

    @Test
    void shouldPreventDefaultBranchFromDeletion() throws IOException {
      mockDeletedBranches(asList("anything", "default"));
      mockExistingBranches(branch("anything"), defaultBranch());

      PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(context, REPOSITORY, PRE_RECEIVE));
      assertThrows(CannotDeleteDefaultBranchException.class, () -> defaultBranchDeleteProtection.protectDefaultBranch(event));
    }

    private void mockExistingBranches(Branch... branches) throws IOException {
      when(branchesCommand.getBranches()).thenReturn(new Branches(branches));
    }

    private Branch defaultBranch() {
      return branch("default", true);
    }

    private Branch branch(String name) {
      return branch(name, false);
    }

    private Branch branch(String name, boolean defaultBranch) {
      return new Branch(name, "1", defaultBranch, 1L);
    }
  }

  private void mockDeletedBranches(List<String> anything) {
    when(context.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(true);
    when(context.getBranchProvider()).thenReturn(branchProvider);
    when(branchProvider.getDeletedOrClosed()).thenReturn(anything);
  }
}
