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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.Repository;
import sonia.scm.store.BlobStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LfsBlobStoreFactory}.
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class LfsBlobStoreFactoryTest {

  @Mock
  private BlobStoreFactory blobStoreFactory;
  
  @InjectMocks
  private LfsBlobStoreFactory lfsBlobStoreFactory;
   
  @Test
  public void getBlobStore() {
    when(blobStoreFactory.withName(any())).thenCallRealMethod();
    Repository repository = new Repository("the-id", "GIT", "space", "the-name");
    lfsBlobStoreFactory.getLfsBlobStore(repository);

    // just make sure the right parameter is passed, as properly validating the return value is nearly impossible with 
    // the return value (and should not be part of this test)
    verify(blobStoreFactory).getStore(argThat(blobStoreParameters -> {
      assertThat(blobStoreParameters.getName()).isEqualTo("git-lfs");
      assertThat(blobStoreParameters.getRepositoryId()).isEqualTo("the-id");
      return true;
    }));

    // make sure there have been no further usages of the factory
    verify(blobStoreFactory, times(1)).getStore(any());
  }
}
