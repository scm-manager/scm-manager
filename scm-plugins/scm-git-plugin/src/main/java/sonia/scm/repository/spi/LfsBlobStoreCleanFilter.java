package sonia.scm.repository.spi;

import org.eclipse.jgit.attributes.FilterCommand;
import org.eclipse.jgit.lfs.Lfs;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.eclipse.jgit.lib.Repository;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;

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


  private Lfs lfsUtil;
  private final BlobStore lfsBlobStore;
  private final Path targetFile;
  private final DigestOutputStream digestOutputStream;

  private long size;

  public LfsBlobStoreCleanFilter(Repository db, InputStream in, OutputStream out, BlobStore lfsBlobStore, Path targetFile)
    throws IOException {
    super(in, out);
    lfsUtil = new Lfs(db);
    this.lfsBlobStore = lfsBlobStore;
    this.targetFile = targetFile;
    Files.createDirectories(lfsUtil.getLfsTmpDir());


   MessageDigest md ;
    try {
      md = MessageDigest.getInstance(LONG_HASH_FUNCTION);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    digestOutputStream = new DigestOutputStream(new OutputStream() {
      @Override
      public void write(int b) {
      }
    }, md);

  }

  @Override
  public int run() throws IOException {
    try {
      byte[] buf = new byte[8192];
      int length = in.read(buf);
      if (length != -1) {
        digestOutputStream.write(buf, 0, length);
        size += length;
        return length;
      } else {
        digestOutputStream.close();
        AnyLongObjectId loid = LongObjectId.fromRaw(digestOutputStream.getMessageDigest().digest());

        Blob existingBlob = lfsBlobStore.get(loid.getName());
        if (existingBlob != null) {
          long blobSize = existingBlob.getSize();
          if (blobSize != size) {
            throw new RuntimeException("lfs entry already exists for loid " + loid.getName() + " but has wrong size");
          }
        } else {
          Blob newBlob = lfsBlobStore.create(loid.getName());
          OutputStream outputStream = newBlob.getOutputStream();
          Files.copy(targetFile, outputStream);
          newBlob.commit();
        }

        LfsPointer lfsPointer = new LfsPointer(loid, size);
        lfsPointer.encode(out);
        in.close();
        out.close();
        return -1;
      }
    } catch (IOException e) {
      if (digestOutputStream != null) {
        digestOutputStream.close();
      }
      in.close();
      out.close();
      throw e;
    }
  }
}
