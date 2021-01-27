package sonia.scm.repository.spi;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import sonia.scm.repository.api.BundleResponse;

import javax.ws.rs.WebApplicationException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class GitBundleCommand extends AbstractGitCommand implements BundleCommand {

  private static final String TAR_ARCHIVE = "tar";

  public GitBundleCommand(GitContext context) {
    super(context);
  }

  @Override
  public BundleResponse bundle(BundleCommandRequest request) throws IOException {
    Path repoDir = context.getDirectory().toPath();
    if (Files.exists(repoDir)) {
      try (OutputStream os = request.getArchive().openStream();
           BufferedOutputStream bos = new BufferedOutputStream(os);
           TarArchiveOutputStream taos = new TarArchiveOutputStream(bos)) {

        createTarEntryForFiles("", repoDir, taos);
        taos.finish();
      }
    }
    else {
      //TODO throw ExportFailedException which already exists on the branch "feature/import_export_with_metadata"
    }
    return new BundleResponse(0);
  }

  @Override
  public String getFileExtension() {
    return TAR_ARCHIVE;
  }

  private void createTarEntryForFiles(String path, Path fileOrDir, TarArchiveOutputStream taos) throws IOException {
    Stream<Path> files = Files.list(fileOrDir);
    if (files != null) {
      files
        .filter(filePath -> !shouldSkipFile(filePath))
        .forEach(f -> bundleFileOrDir(path, f, taos));
    }
  }

  private void bundleFileOrDir(String path, Path fileOrDir, TarArchiveOutputStream taos) {
    try {
      String filePath = path + "/" + fileOrDir.getFileName().toString();
      if (Files.isDirectory(fileOrDir)) {
        createTarEntryForFiles(filePath, fileOrDir, taos);
      } else {
        createArchiveEntryForFile(filePath, fileOrDir, taos);
      }
    } catch (IOException e) {
      //TODO throw ExportFailedException which already exists on the branch "feature/import_export_with_metadata"
      throw new WebApplicationException();
    }
  }

  private boolean shouldSkipFile(Path filePath) {
    return filePath.getFileName().toString().equals("config");
  }

  private void createArchiveEntryForFile(String filePath, Path path, TarArchiveOutputStream taos) throws IOException {
    TarArchiveEntry entry = new TarArchiveEntry(filePath);
    entry.setSize(path.toFile().length());
    taos.putArchiveEntry(entry);
    Files.copy(path, taos);
    taos.closeArchiveEntry();
  }
}
