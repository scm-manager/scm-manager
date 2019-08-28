package sonia.scm.repository.spi;

import java.io.File;

public interface ModifyCommand {
  String commit();

  void delete(String toBeDeleted);

  void create(String toBeCreated, File file);

  void modify(String path, File file);

  void move(String sourcePath, String targetPath);
}
