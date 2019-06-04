package sonia.scm.update.repository;

import com.google.inject.Injector;

import java.nio.file.Path;

public enum MigrationStrategy {

  COPY(CopyMigrationStrategy.class),
  MOVE(MoveMigrationStrategy.class),
  INLINE(InlineMigrationStrategy.class);

  private Class<? extends Instance> implementationClass;

  MigrationStrategy(Class<? extends Instance> implementationClass) {
    this.implementationClass = implementationClass;
  }

  Instance from(Injector injector) {
    return injector.getInstance(implementationClass);
  }

  interface Instance {
    Path migrate(String id, String name, String type);
  }
}
