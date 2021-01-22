package sonia.scm.repository.spi;

import com.google.common.io.ByteSource;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.UnbundleResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

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

    File repositoryDir = context.getDirectory();
    LOG.debug("archive repository {} to {}", repositoryDir,
      archive);

    if (!context.getDirectory().exists()) {
      context.getDirectory().mkdirs();
    }

    try (TarArchiveInputStream tais = new TarArchiveInputStream(request.getArchive().openBufferedStream())) {
      TarArchiveEntry entry = tais.getNextTarEntry();
      while (entry != null) {
        createDirectoriesIfNestedFile(repositoryDir, entry);
        File file = new File(repositoryDir, entry.getName());
        if (!file.exists()) {
          file.createNewFile();
        }
        Files.copy(tais, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        entry = tais.getNextTarEntry();
      }
    }

    return new UnbundleResponse(0);
  }

  private void createDirectoriesIfNestedFile(File repositoryDir, TarArchiveEntry entry) {
    if (entry.getName().contains(File.separator)) {
      String[] split = entry.getName().split(File.separator);
      String[] splittedFileDirPath = Arrays.copyOf(split, split.length - 1);
      String existingDirPath = "";
      for (String dir : splittedFileDirPath) {
        File dirPath = new File(repositoryDir, existingDirPath + File.separator + dir);
        if (!dirPath.exists()) {
          dirPath.mkdir();
        }
        existingDirPath += File.separator + dir;
      }
    }
  }
}
