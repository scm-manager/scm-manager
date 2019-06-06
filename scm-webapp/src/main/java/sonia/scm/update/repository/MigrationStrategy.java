package sonia.scm.update.repository;

import com.google.inject.Injector;

import java.nio.file.Path;

public enum MigrationStrategy {

  COPY(CopyMigrationStrategy.class,
    "Copy the repository data files to the new native location inside SCM-Manager home directory. " +
      "This will keep the original directory."),
  MOVE(MoveMigrationStrategy.class,
    "Move the repository data files to the new native location inside SCM-Manager home directory. " +
      "The original directory will be deleted."),
  INLINE(InlineMigrationStrategy.class,
    "Use the current directory where the repository data files are stored, but modify the directory " +
      "structure so that it can be used for SCM-Manager v2. The repository data files will be moved to a new " +
      "subdirectory 'data' inside the current directory.");

  private final Class<? extends Instance> implementationClass;
  private final String description;

  MigrationStrategy(Class<? extends Instance> implementationClass, String description) {
    this.implementationClass = implementationClass;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  Instance from(Injector injector) {
    return injector.getInstance(implementationClass);
  }

  interface Instance {
    Path migrate(String id, String name, String type);
  }
}
