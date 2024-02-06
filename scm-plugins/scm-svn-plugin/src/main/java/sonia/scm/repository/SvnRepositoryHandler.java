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
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSHooks;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.util.SVNDebugLog;
import sonia.scm.logging.SVNKitLogger;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.repository.spi.SvnRepositoryServiceProvider;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.util.Util;

import java.io.File;
import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;


@Singleton
@Extension
public class SvnRepositoryHandler extends AbstractSimpleRepositoryHandler<SvnConfig> {

  public static final String PROPERTY_UUID = "svn.uuid";
  public static final String RESOURCE_VERSION = "sonia/scm/version/scm-svn-plugin";
  public static final String TYPE_DISPLAYNAME = "Subversion";
  public static final String TYPE_NAME = "svn";

  public static final RepositoryType TYPE = new RepositoryType(TYPE_NAME, TYPE_DISPLAYNAME, SvnRepositoryServiceProvider.COMMANDS);

  private static final Logger LOG = LoggerFactory.getLogger(SvnRepositoryHandler.class);
  private SvnRepositoryHook hook;

  @Inject
  public SvnRepositoryHandler(ConfigurationStoreFactory storeFactory,
                              HookEventFacade eventFacade,
                              RepositoryLocationResolver repositoryLocationResolver,
                              PluginLoader pluginLoader) {
    super(storeFactory, repositoryLocationResolver, pluginLoader);

    // register logger
    SVNDebugLog.setDefaultLog(new SVNKitLogger());

    // setup FSRepositoryFactory for SvnRepositoryBrowser
    FSRepositoryFactory.setup();

    // register hook
    if (eventFacade != null) {
      hook = new SvnRepositoryHook(eventFacade, this);
      FSHooks.registerHook(hook);
    } else if (LOG.isWarnEnabled()) {
      LOG.warn(
        "unable to register hook, beacause of missing repositorymanager");
    }
  }

  @Override
  public ImportHandler getImportHandler() {
    return new SvnImportHandler(this);
  }

  @Override
  public RepositoryType getType() {
    return TYPE;
  }

  @Override
  public String getVersionInformation() {
    return getStringFromResource(RESOURCE_VERSION, DEFAULT_VERSION_INFORMATION);
  }

  @Override
  protected void create(Repository repository, File directory) throws InternalRepositoryException {

    SVNRepository svnRepository = null;

    try {
      SVNURL url = createSvnUrl(directory);

      svnRepository = SVNRepositoryFactory.create(url);

      String uuid = svnRepository.getRepositoryUUID(true);

      if (Util.isNotEmpty(uuid)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("store repository uuid {} for {}", uuid,
            repository.getName());
        }

        repository.setProperty(PROPERTY_UUID, uuid);
      } else if (LOG.isWarnEnabled()) {
        LOG.warn("could not read repository uuid for {}",
          repository.getName());
      }
    } catch (SVNException ex) {
      throw new InternalRepositoryException(repository, "could not create repository", ex);
    } finally {
      SvnUtil.closeSession(svnRepository);
    }
  }

  public SVNURL createSvnUrl(File directory) {
    Compatibility comp = config.getCompatibility();

    if (LOG.isDebugEnabled()) {

      LOG.debug("create svn repository \"{}\": " +
          "pre14Compatible={}, " +
          "pre15Compatible={}, " +
          "pre16Compatible={}, " +
          "pre17Compatible={}, " +
          "with17Compatible={}",
        directory.getName(),
        comp.isPre14Compatible(),
        comp.isPre15Compatible(),
        comp.isPre16Compatible(),
        comp.isPre17Compatible(),
        comp.isWith17Compatible());
    }
    try {
      return SVNRepositoryFactory.createLocalRepository(directory, null,
        true, false, comp.isPre14Compatible(),
        comp.isPre15Compatible(), comp.isPre16Compatible(),
        comp.isPre17Compatible(), comp.isWith17Compatible());
    } catch (SVNException ex) {
      throw new InternalRepositoryException(entity(File.class, directory.toString()), "could not create svn url", ex);
    }
  }

  @Override
  protected SvnConfig createInitialConfig() {
    return new SvnConfig();
  }

  @Override
  protected Class<SvnConfig> getConfigClass() {
    return SvnConfig.class;
  }

  @Override
  protected void postCreate(Repository repository, File directory) throws IOException {
    new SvnConfigHelper().writeRepositoryId(repository, directory);
  }

  String getRepositoryId(File directory) {
    return new SvnConfigHelper().getRepositoryId(directory);
  }

  @Override
  public void close() throws IOException {
    if (hook != null) {
      FSHooks.unregisterHook(hook);
    }
    super.close();
  }
}
