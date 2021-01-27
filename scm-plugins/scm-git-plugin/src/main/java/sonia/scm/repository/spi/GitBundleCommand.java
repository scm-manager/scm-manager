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
        .forEach(f -> processFileOrDir(path, f, taos));
    }
  }

  private void processFileOrDir(String path, Path fileOrDir, TarArchiveOutputStream taos) {
    try {
      if (Files.isDirectory(fileOrDir)) {
        String filePath = path + "/" + fileOrDir.getFileName().toString();
        createTarEntryForFiles(filePath, fileOrDir, taos);
      } else {
        createArchiveEntryForFile(path, fileOrDir, taos);
      }
    } catch (IOException e) {
      //TODO fix exception / the matching exception already exists on the branch "feature/import_export_with_metadata"
      throw new WebApplicationException();
    }
  }

  private boolean shouldSkipFile(Path filePath) {
    return filePath.getFileName().toString().equals("config");
  }

  private void createArchiveEntryForFile(String filepath, Path filePath, TarArchiveOutputStream taos) throws IOException {
    TarArchiveEntry entry = new TarArchiveEntry(filepath + "/" + filePath.getFileName().toString());
    entry.setSize(filePath.toFile().length());
    taos.putArchiveEntry(entry);
    Files.copy(filePath, taos);
    taos.closeArchiveEntry();
  }
}
