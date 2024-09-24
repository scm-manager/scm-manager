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

package sonia.scm.web.lfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.StoreType;
import sonia.scm.update.RepositoryUpdateIterator;
import sonia.scm.update.StoreUpdateStepUtilFactory;

import java.io.IOException;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RemoveRepositoryIdFromBlobStoreUpdateStepTest {

  @Mock
  private RepositoryUpdateIterator repositoryUpdateIterator;
  @Mock(answer = Answers.CALLS_REAL_METHODS)
  private StoreUpdateStepUtilFactory utilFactory;
  @Mock
  private StoreUpdateStepUtilFactory.StoreUpdateStepUtil util;

  @InjectMocks
  private RemoveRepositoryIdFromBlobStoreUpdateStep updateStep;

  @Test
  void migrateBlobsFromOldStoreToNewStore() throws IOException {
    Mockito.doAnswer(invocation -> {
      invocation.getArgument(0, Consumer.class).accept("repo-id");
      return null;
    }).when(repositoryUpdateIterator).forEachRepository(any());

    doReturn(util)
      .when(utilFactory).build(eq(StoreType.BLOB), argThat(argument -> argument.getName().equals("repo-id-git-lfs")));

    updateStep.doUpdate();

    verify(util).renameStore("git-lfs");
  }
}
