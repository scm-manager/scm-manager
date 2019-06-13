package sonia.scm.update.repository;

import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.empty;

public class IgnoreMigrationStrategy implements MigrationStrategy.Instance {
  @Override
  public Optional<Path> migrate(String id, String name, String type) {
    return empty();
  }
}
