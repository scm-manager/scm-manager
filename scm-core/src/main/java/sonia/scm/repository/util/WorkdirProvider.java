package sonia.scm.repository.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class WorkdirProvider {

  private final File poolDirectory;

  public WorkdirProvider() {
    this(new File(System.getProperty("scm.workdir" , System.getProperty("java.io.tmpdir")), "scm-work"));
  }

  public WorkdirProvider(File poolDirectory) {
    this.poolDirectory = poolDirectory;
    if (!poolDirectory.exists() && !poolDirectory.mkdirs()) {
      throw new IllegalStateException("could not create pool directory " + poolDirectory);
    }
  }

  public File createNewWorkdir() {
    try {
      return Files.createTempDirectory(poolDirectory.toPath(),"workdir").toFile();
    } catch (IOException e) {
      throw new RuntimeException("could not create temporary workdir", e);
    }
  }
}
