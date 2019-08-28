package sonia.scm.repository.spi;

import java.io.File;
import java.io.IOException;

public interface ModifyCommand {

  String execute(ModifyCommandRequest request);

  interface Worker {
    void delete(String toBeDeleted);

    void create(String toBeCreated, File file) throws IOException;

    void modify(String path, File file);

    void move(String sourcePath, String targetPath);
  }
}
