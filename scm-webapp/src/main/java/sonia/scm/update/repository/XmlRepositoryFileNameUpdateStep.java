/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.update.repository;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.store.StoreConstants;
import sonia.scm.version.Version;

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
  private final XmlRepositoryDAO repositoryDAO;

  @Inject
  public XmlRepositoryFileNameUpdateStep(SCMContextProvider contextProvider, XmlRepositoryDAO repositoryDAO) {
    this.contextProvider = contextProvider;
    this.repositoryDAO = repositoryDAO;
  }

  @Override
  public void doUpdate() throws IOException {
    Path configDir = contextProvider.getBaseDirectory().toPath().resolve(StoreConstants.CONFIG_DIRECTORY_NAME);
    Path oldRepositoriesFile = configDir.resolve("repositories.xml");
    Path newRepositoryPathsFile = configDir.resolve(PathBasedRepositoryLocationResolver.STORE_NAME + StoreConstants.FILE_EXTENSION);
    if (Files.exists(oldRepositoriesFile)) {
      LOG.info("moving old repositories database files to repository-paths file");
      Files.move(oldRepositoriesFile, newRepositoryPathsFile);
      repositoryDAO.refresh();
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
