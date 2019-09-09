package sonia.scm.repository.spi;

import java.io.File;
import java.io.IOException;

public interface ModifyCommand {

  String execute(ModifyCommandRequest request);

  interface Worker {
    void delete(String toBeDeleted) throws IOException;

    void create(String toBeCreated, File file, boolean overwrite) throws IOException;

    void modify(String path, File file) throws IOException;

    void move(String sourcePath, String targetPath);
  }
}
