package sonia.scm.repository.spi;

import com.google.common.io.ByteSource;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.api.UnbundleResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.google.common.base.Preconditions.checkNotNull;

public class GitUnbundleCommand extends AbstractGitCommand implements UnbundleCommand {

  private static final Logger LOG = LoggerFactory.getLogger(GitUnbundleCommand.class);

  GitUnbundleCommand(GitContext context) {
    super(context);
  }

  @Override
  public UnbundleResponse unbundle(UnbundleCommandRequest request) throws IOException {
    ByteSource archive = checkNotNull(request.getArchive(),
      "archive is required");

    Path repositoryDir = context.getDirectory().toPath();
    LOG.debug("archive repository {} to {}", repositoryDir, archive);

    if (!Files.exists(repositoryDir)) {
      Files.createDirectories(repositoryDir);
    }

    try (TarArchiveInputStream tais = new TarArchiveInputStream(request.getArchive().openBufferedStream())) {
      TarArchiveEntry entry = tais.getNextTarEntry();
      while (entry != null) {
        createDirectoriesIfNestedFile(repositoryDir, entry);
        Path filePath = repositoryDir.resolve(entry.getName());
        if (!Files.exists(filePath)) {
          Files.createFile(filePath);
        }
        Files.copy(tais, filePath, StandardCopyOption.REPLACE_EXISTING);
        entry = tais.getNextTarEntry();
      }
    }
    return new UnbundleResponse(0);
  }

  private void createDirectoriesIfNestedFile(Path repositoryDir, TarArchiveEntry entry) throws IOException {
    if (entry.getName().contains("/")) {
      Path filePath = Paths.get(entry.getName());
      if (!Files.exists(filePath)) {
          Files.createDirectories(repositoryDir.resolve(filePath));
        }
    }
  }
}
