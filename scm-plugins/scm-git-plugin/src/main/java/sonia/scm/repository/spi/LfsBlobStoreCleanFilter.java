package sonia.scm.repository.spi;

import com.google.common.io.ByteStreams;
import org.eclipse.jgit.attributes.FilterCommand;
import org.eclipse.jgit.lfs.Lfs;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.eclipse.jgit.lib.Repository;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.eclipse.jgit.lfs.lib.Constants.LONG_HASH_FUNCTION;

/**
 * Adapted version of JGit's {@link org.eclipse.jgit.lfs.CleanFilter} to write the
 * lfs file directly to the lfs blob store.
 */
public class LfsBlobStoreCleanFilter extends FilterCommand {

  private static final Logger LOG = LoggerFactory.getLogger(LfsBlobStoreCleanFilter.class);

  private Lfs lfsUtil;
  private final BlobStore lfsBlobStore;
  private final Path targetFile;

  public LfsBlobStoreCleanFilter(Repository db, InputStream in, OutputStream out, BlobStore lfsBlobStore, Path targetFile)
    throws IOException {
    super(in, out);
    lfsUtil = new Lfs(db);
    this.lfsBlobStore = lfsBlobStore;
    this.targetFile = targetFile;
    Files.createDirectories(lfsUtil.getLfsTmpDir());
  }

  @Override
  public int run() throws IOException {
    LOG.info("running scm lfs filter for file {}", targetFile);
    DigestOutputStream digestOutputStream = createDigestStream();
    try {
      long size = ByteStreams.copy(in, digestOutputStream);
      AnyLongObjectId loid = LongObjectId.fromRaw(digestOutputStream.getMessageDigest().digest());
      String hash = loid.getName();

      Blob existingBlob = lfsBlobStore.get(hash);
      if (existingBlob != null) {
        LOG.info("found existing lfs blob for oid {}", hash);
        long blobSize = existingBlob.getSize();
        if (blobSize != size) {
          // Mathematicians say this will never happen
          throw new RuntimeException("lfs entry already exists for loid " + hash + " but has wrong size");
        }
      } else {
        LOG.info("uploading new lfs blob for oid {}", hash);
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
    MessageDigest md ;
    try {
      md = MessageDigest.getInstance(LONG_HASH_FUNCTION);
    } catch (NoSuchAlgorithmException e) {
      // Yes there is such a hash function (should be sha256)
      throw new RuntimeException(e);
    }
    return new DigestOutputStream(new OutputStream() {
      @Override
      public void write(int b) {
        // no further target here, we are just interested in the digest
      }
    }, md);
  }
}
