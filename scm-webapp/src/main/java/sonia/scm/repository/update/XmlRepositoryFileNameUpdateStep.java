package sonia.scm.repository.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver;
import sonia.scm.store.StoreConstants;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static sonia.scm.version.Version.parse;

/**
 * Moves an existing <code>repositories.xml</code> file to <code>repository-paths.xml</code>.
 * Note that this has to run <em>after</em> an old v1 repository database has been migrated to v2
 * (see {@link XmlRepositoryV1UpdateStep}).
 */
@Extension
public class XmlRepositoryFileNameUpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(XmlRepositoryFileNameUpdateStep.class);

  private final SCMContextProvider contextProvider;

  @Inject
  public XmlRepositoryFileNameUpdateStep(SCMContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public void doUpdate() throws IOException {
    Path configDir = contextProvider.getBaseDirectory().toPath().resolve(StoreConstants.CONFIG_DIRECTORY_NAME);
    Path oldRepositoriesFile = configDir.resolve("repositories.xml");
    Path newRepositoryPathsFile = configDir.resolve(PathBasedRepositoryLocationResolver.STORE_NAME + StoreConstants.FILE_EXTENSION);
    if (Files.exists(oldRepositoriesFile)) {
      LOG.info("moving old repositories database files to repository-paths file");
      Files.move(oldRepositoriesFile, newRepositoryPathsFile);
    }
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.1");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.repository.xml";
  }
}
