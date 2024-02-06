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

package sonia.scm.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.autoconfig.AutoConfigurator;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.spi.HgRepositoryServiceProvider;
import sonia.scm.repository.spi.HgVersionCommand;
import sonia.scm.repository.spi.HgWorkingCopyFactory;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Singleton
@Extension
public class HgRepositoryHandler
  extends AbstractSimpleRepositoryHandler<HgGlobalConfig> {

  public static final String TYPE_DISPLAYNAME = "Mercurial";
  public static final String TYPE_NAME = "hg";
  public static final RepositoryType TYPE = new RepositoryType(
    TYPE_NAME,
    TYPE_DISPLAYNAME,
    HgRepositoryServiceProvider.COMMANDS,
    HgRepositoryServiceProvider.FEATURES
  );

  private static final Logger logger = LoggerFactory.getLogger(HgRepositoryHandler.class);

  public static final String PATH_HGRC = ".hg".concat(File.separator).concat("hgrc");
  private static final String CONFIG_SECTION_SCMM = "scmm";
  private static final String CONFIG_KEY_REPOSITORY_ID = "repositoryid";

  private final HgWorkingCopyFactory workingCopyFactory;
  private final AutoConfigurator configurator;

  @Inject
  public HgRepositoryHandler(ConfigurationStoreFactory storeFactory,
                             RepositoryLocationResolver repositoryLocationResolver,
                             PluginLoader pluginLoader, HgWorkingCopyFactory workingCopyFactory, AutoConfigurator configurator) {
    super(storeFactory, repositoryLocationResolver, pluginLoader);
    this.workingCopyFactory = workingCopyFactory;
    this.configurator = configurator;
  }

  public void doAutoConfiguration(HgGlobalConfig autoConfig) {
    configurator.configure(autoConfig);
  }

  @Override
  public void init(SCMContextProvider context) {
    super.init(context);
    writeHgExtensions(context);
  }

  @Override
  public void loadConfig() {
    super.loadConfig();

    if (config == null) {
      config = new HgGlobalConfig();
      storeConfig();
    }

    if (!isConfigValid(config)) {
      doAutoConfiguration(config);
      storeConfig();
    }
  }

  private boolean isConfigValid(HgGlobalConfig config) {
    return config.isValid() && new HgVerifier().isValid(config);
  }

  @Override
  public ImportHandler getImportHandler() {
    return new HgImportHandler(this);
  }

  @Override
  public RepositoryType getType() {
    return TYPE;
  }

  @Override
  public String getVersionInformation() {
    return getVersionInformation(new HgVersionCommand(getConfig()));
  }

  String getVersionInformation(HgVersionCommand command) {
    return String.format("scm-hg-version/%s %s",
      SCMContext.getContext().getVersion(),
      command.get()
    );
  }

  @Override
  protected ExtendedCommand buildCreateCommand(Repository repository,
                                               File directory) {
    ExtendedCommand cmd = new ExtendedCommand(config.getHgBinary(), "init",
      directory.getAbsolutePath());

    // copy system environment, because of the PATH variable
    cmd.setUseSystemEnvironment(true);

    // issue-97
    cmd.setWorkDirectory(baseDirectory);

    return cmd;
  }

  /**
   * Writes repository to .hg/hgrc.
   */
  @Override
  protected void postCreate(Repository repository, File directory)
    throws IOException {
    File hgrcFile = new File(directory, PATH_HGRC);
    INIConfiguration hgrc = new INIConfiguration();

    INISection iniSection = new INISection(CONFIG_SECTION_SCMM);
    iniSection.setParameter(CONFIG_KEY_REPOSITORY_ID, repository.getId());
    INIConfiguration iniConfiguration = new INIConfiguration();
    iniConfiguration.addSection(iniSection);
    hgrc.addSection(iniSection);

    INIConfigurationWriter writer = new INIConfigurationWriter();

    writer.write(hgrc, hgrcFile);
  }

  @Override
  protected Class<HgGlobalConfig> getConfigClass() {
    return HgGlobalConfig.class;
  }

  private void writeHgExtensions(SCMContextProvider context) {
    IOUtil.mkdirs(HgExtensions.getScriptDirectory(context));

    for (HgExtensions script : HgExtensions.values()) {
      if (logger.isDebugEnabled()) {
        logger.debug("write python script {}", script.getName());
      }

      try (InputStream content = input(script); OutputStream output = output(context, script)) {
        IOUtil.copy(content, output);
      } catch (IOException ex) {
        throw new IllegalStateException("could not write hg extension", ex);
      }
    }
  }

  private InputStream input(HgExtensions script) {
    return HgRepositoryHandler.class.getResourceAsStream(script.getResourcePath());
  }

  private OutputStream output(SCMContextProvider context, HgExtensions script) throws FileNotFoundException {
    return new FileOutputStream(script.getFile(context));
  }

  public HgWorkingCopyFactory getWorkingCopyFactory() {
    return workingCopyFactory;
  }

}
