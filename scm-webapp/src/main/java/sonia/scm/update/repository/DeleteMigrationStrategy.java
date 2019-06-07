package sonia.scm.update.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.util.IOUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class DeleteMigrationStrategy extends BaseMigrationStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(DeleteMigrationStrategy.class);

  @Inject
  DeleteMigrationStrategy(SCMContextProvider contextProvider) {
    super(contextProvider);
  }

  @Override
  public Optional<Path> migrate(String id, String name, String type) {
    Path sourceDataPath = getSourceDataPath(name, type);
    try {
      IOUtil.delete(sourceDataPath.toFile(), true);
    } catch (IOException e) {
      LOG.warn("could not delete old repository path for repository {} with type {} and id {}", name, type, id);
    }
    return Optional.empty();
  }
}
