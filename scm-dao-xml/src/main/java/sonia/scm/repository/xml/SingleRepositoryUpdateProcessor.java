package sonia.scm.repository.xml;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public class SingleRepositoryUpdateProcessor {

  @Inject
  private PathBasedRepositoryLocationResolver locationResolver;

  public void doUpdate(BiConsumer<String, Path> forEachRepository) {
    locationResolver.forAllPaths(forEachRepository);
  }
}
