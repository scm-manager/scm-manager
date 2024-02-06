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
