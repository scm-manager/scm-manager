package sonia.scm.web.lfs;

import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.update.BlobDirectoryAccess;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.nio.file.Path;

@Extension
public class LfsV1UpdateStep implements UpdateStep {

  private final BlobDirectoryAccess blobDirectoryAccess;

  @Inject
  public LfsV1UpdateStep(BlobDirectoryAccess blobDirectoryAccess) {
    this.blobDirectoryAccess = blobDirectoryAccess;
  }

  @Override
  public void doUpdate() throws Exception {
    blobDirectoryAccess.forBlobDirectories(
      f -> {
        Path v1Directory = f.getFileName();
        String v1DirectoryName = v1Directory.toString();
        if (v1DirectoryName.endsWith("-git-lfs")) {
          blobDirectoryAccess.moveToRepositoryBlobStore(f, v1DirectoryName, v1DirectoryName.substring(0, v1DirectoryName.length() - "-git-lfs".length()));
        }
      }
    );
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.git.lfs";
  }
}
