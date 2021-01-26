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

import sonia.scm.migration.UpdateException;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.update.RepositoryUpdateIterator;
import sonia.scm.util.IOUtil;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static sonia.scm.version.Version.parse;

@Extension
public class RemoveRepositoryIdFromBlobStoreUpdateStep implements UpdateStep {

  private final RepositoryUpdateIterator repositoryUpdateIterator;
  private final BlobStoreFactory blobStoreFactory;

  @Inject
  public RemoveRepositoryIdFromBlobStoreUpdateStep(RepositoryUpdateIterator repositoryUpdateIterator, BlobStoreFactory blobStoreFactory) {
    this.repositoryUpdateIterator = repositoryUpdateIterator;
    this.blobStoreFactory = blobStoreFactory;
  }

  @Override
  public void doUpdate()  {
    repositoryUpdateIterator.forEachRepository(this::doUpdate);
  }

  private void doUpdate(String repositoryId) {
    BlobStore oldBlobStore = blobStoreFactory.withName(repositoryId + "-git-lfs").forRepository(repositoryId).build();
    BlobStore newBlobStore = blobStoreFactory.withName("git-lfs").forRepository(repositoryId).build();
    List<Blob> allBlobs = oldBlobStore.getAll();
    allBlobs.forEach(
      blob -> migrateBlob(oldBlobStore, newBlobStore, blob, repositoryId)
    );
  }

  private void migrateBlob(BlobStore oldBlobStore, BlobStore newBlobStore, Blob oldBlob, String repositoryId) {
    try {
      Blob newBlob = newBlobStore.create(oldBlob.getId());
      OutputStream outputStream = newBlob.getOutputStream();
      IOUtil.copy(oldBlob.getInputStream(), outputStream);
      newBlob.commit();
      oldBlobStore.remove(oldBlob);
    } catch (IOException e) {
      throw new UpdateException(String.format("could not move old lfs blob %s for repository id %s", oldBlob.getId(), repositoryId));
    }
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.1");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.git.lfs";
  }
}
