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
    
package sonia.scm.repository.spi;

import com.google.common.io.ByteStreams;
import org.eclipse.jgit.attributes.FilterCommand;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.Constants;
import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;

/**
 * Adapted version of JGit's {@link org.eclipse.jgit.lfs.CleanFilter} to write the
 * lfs file directly to the lfs blob store.
 */
class LfsBlobStoreCleanFilter extends FilterCommand {

  private static final Logger LOG = LoggerFactory.getLogger(LfsBlobStoreCleanFilter.class);

  private final BlobStore lfsBlobStore;
  private final Path targetFile;

  LfsBlobStoreCleanFilter(InputStream in, OutputStream out, BlobStore lfsBlobStore, Path targetFile) {
    super(in, out);
    this.lfsBlobStore = lfsBlobStore;
    this.targetFile = targetFile;
  }

  @Override
  // Suppress warning for RuntimeException after check for wrong size, because mathematicians say this will never happen
  @SuppressWarnings("squid:S00112")
  public int run() throws IOException {
    LOG.debug("running scm lfs filter for file {}", targetFile);
    DigestOutputStream digestOutputStream = createDigestStream();
    try {
      long size = ByteStreams.copy(in, digestOutputStream);
      AnyLongObjectId loid = LongObjectId.fromRaw(digestOutputStream.getMessageDigest().digest());
      String hash = loid.getName();

      Blob existingBlob = lfsBlobStore.get(hash);
      if (existingBlob != null) {
        LOG.debug("found existing lfs blob for oid {}", hash);
        long blobSize = existingBlob.getSize();
        if (blobSize != size) {
          throw new RuntimeException("lfs entry already exists for loid " + hash + " but has wrong size");
        }
      } else {
        LOG.debug("uploading new lfs blob for oid {}", hash);
        Blob newBlob = lfsBlobStore.create(hash);
        OutputStream outputStream = newBlob.getOutputStream();
        Files.copy(targetFile, outputStream);
        newBlob.commit();
      }

      LfsPointer lfsPointer = new LfsPointer(loid, size);
      lfsPointer.encode(out);
      return -1;
    } finally {
      IOUtil.close(digestOutputStream);
      IOUtil.close(in);
      IOUtil.close(out);
    }
  }

  private DigestOutputStream createDigestStream() {
    return new DigestOutputStream(new OutputStream() {
      @Override
      public void write(int b) {
        // no further target here, we are just interested in the digest
      }
    }, Constants.newMessageDigest());
  }
}
