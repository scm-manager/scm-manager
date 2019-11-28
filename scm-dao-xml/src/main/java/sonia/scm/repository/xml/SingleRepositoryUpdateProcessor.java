package sonia.scm.repository.xml;

import sonia.scm.repository.RepositoryLocationResolver;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public class SingleRepositoryUpdateProcessor {

  @Inject
  private RepositoryLocationResolver locationResolver;

  public void doUpdate(BiConsumer<String, Path> forEachRepository) {
    locationResolver.forClass(Path.class).forAllLocations(forEachRepository);
  }
}
