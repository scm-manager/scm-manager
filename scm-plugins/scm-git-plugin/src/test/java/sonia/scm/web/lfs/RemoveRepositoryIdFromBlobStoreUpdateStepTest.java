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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.update.RepositoryUpdateIterator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveRepositoryIdFromBlobStoreUpdateStepTest {

  @Mock
  private RepositoryUpdateIterator repositoryUpdateIterator;
  @Mock(answer = Answers.CALLS_REAL_METHODS)
  private BlobStoreFactory blobStoreFactory;
  @Mock
  private BlobStore oldBlobStore;
  @Mock
  private BlobStore newBlobStore;

  @InjectMocks
  private RemoveRepositoryIdFromBlobStoreUpdateStep updateStep;

  private Map<String, ByteArrayOutputStream> blobStreams = new HashMap<>();

  @Test
  void migrateBlobsFromOldStoreToNewStore() throws IOException {
    Mockito.doAnswer(invocation -> {
      invocation.getArgument(0, Consumer.class).accept("repo-id");
      return null;
    }).when(repositoryUpdateIterator).forEachRepository(any());

    doReturn(oldBlobStore)
      .when(blobStoreFactory).getStore(argThat(argument -> argument.getName().startsWith("repo-id")));
    doReturn(newBlobStore)
      .when(blobStoreFactory).getStore(argThat(argument -> argument.getName().equals("git-lfs")));

    Blob oldBlob1 = createBlob("blob1");
    Blob oldBlob2 = createBlob("blob2");
    when(oldBlobStore.getAll()).thenReturn(asList(oldBlob1, oldBlob2));
    Blob newBlob1 = createBlob("newBlob1");
    Blob newBlob2 = createBlob("newBlob2");
    when(newBlobStore.create("blob1")).thenReturn(newBlob1);
    when(newBlobStore.create("blob2")).thenReturn(newBlob2);

    updateStep.doUpdate();

    verify(newBlobStore).create("blob1");
    verify(newBlobStore).create("blob2");

    verify(oldBlobStore).remove(oldBlob1);
    verify(oldBlobStore).remove(oldBlob2);

    Assertions.assertThat(blobStreams.get("newBlob1")).hasToString("some data for blob1");
    Assertions.assertThat(blobStreams.get("newBlob2")).hasToString("some data for blob2");
  }

  private Blob createBlob(String id) throws IOException {
    Blob blob = mock(Blob.class);
    lenient().when(blob.getId()).thenReturn(id);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    blobStreams.put(id, outputStream);
    lenient().when(blob.getOutputStream()).thenReturn(outputStream);
    String blobContent = "some data for " + id;
    lenient().when(blob.getInputStream()).thenReturn(new ByteArrayInputStream(blobContent.getBytes(StandardCharsets.UTF_8)));
    return blob;
  }
}
