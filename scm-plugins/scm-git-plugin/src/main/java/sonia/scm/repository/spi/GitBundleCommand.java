package sonia.scm.repository.spi;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import sonia.scm.repository.api.BundleResponse;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class GitBundleCommand extends AbstractGitCommand implements BundleCommand {

  private static final String TAR_ARCHIVE = "tar";

  public GitBundleCommand(GitContext context) {
    super(context);
  }

  @Override
  public BundleResponse bundle(BundleCommandRequest request) throws IOException {

    File repoDir = context.getDirectory();

    if (repoDir.exists()) {
      try (OutputStream os = request.getArchive().openStream();
           BufferedOutputStream bos = new BufferedOutputStream(os);
           TarArchiveOutputStream taos = new TarArchiveOutputStream(bos)) {

        createTarEntryForFiles("", repoDir, taos);
        taos.finish();
      }
    }
    return new BundleResponse(0);
  }

  @Override
  public String getFileExtension() {
    return TAR_ARCHIVE;
  }

  private void createTarEntryForFiles(String path, File fileOrDir, TarArchiveOutputStream taos) throws IOException {
    File[] files = fileOrDir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          String filePath = path + file.getName();
          createTarEntryForFiles(filePath, file, taos);
        } else {
          TarArchiveEntry entry = new TarArchiveEntry(path + File.separator + file.getName());
          entry.setSize(file.length());
          taos.putArchiveEntry(entry);
          Files.copy(file.toPath(), taos);
          taos.closeArchiveEntry();
        }
      }
    }
  }
}
