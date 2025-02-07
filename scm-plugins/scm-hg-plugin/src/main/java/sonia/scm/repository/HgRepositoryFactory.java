/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.javahg.RepositoryConfiguration;
import org.javahg.ext.purge.PurgeExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.hooks.HookEnvironment;
import sonia.scm.repository.spi.javahg.HgConfigFileExtension;
import sonia.scm.repository.spi.javahg.HgFileviewExtension;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

@Singleton
public class HgRepositoryFactory {

  private static final Logger LOG = LoggerFactory.getLogger(HgRepositoryFactory.class);

  private final HgConfigResolver configResolver;
  private final HookEnvironment hookEnvironment;
  private final HgEnvironmentBuilder environmentBuilder;

  @Inject
  public HgRepositoryFactory(HgConfigResolver configResolver, HookEnvironment hookEnvironment, HgEnvironmentBuilder environmentBuilder) {
    this.configResolver = configResolver;
    this.hookEnvironment = hookEnvironment;
    this.environmentBuilder = environmentBuilder;
  }

  public org.javahg.Repository openForRead(Repository repository) {
    return open(repository, environmentBuilder.read(repository));
  }

  public org.javahg.Repository openForWrite(Repository repository) {
    return open(repository, environmentBuilder.write(repository));
  }

  private org.javahg.Repository open(Repository repository, Map<String, String> environment) {
    HgConfig config = configResolver.resolve(repository);
    File directory = config.getDirectory();

    RepositoryConfiguration repoConfiguration = new RepositoryConfiguration();
    repoConfiguration.setHgrcPath(null);
    repoConfiguration.getEnvironment().putAll(environment);
    repoConfiguration.addExtension(HgFileviewExtension.class);
    repoConfiguration.addExtension(PurgeExtension.class);
    repoConfiguration.addExtension(HgConfigFileExtension.class);

    boolean pending = hookEnvironment.isPending();
    repoConfiguration.setEnablePendingChangesets(pending);

    Charset encoding = encoding(config);
    repoConfiguration.setEncoding(encoding);

    repoConfiguration.setHgBin(config.getHgBinary());

    LOG.trace("open hg repository {}: encoding: {}, pending: {}", directory, encoding, pending);

    return org.javahg.Repository.open(repoConfiguration, directory);
  }

  private Charset encoding(HgConfig config) {
    String charset = config.getEncoding();
    try {
      return Charset.forName(charset);
    } catch (UnsupportedCharsetException ex) {
      LOG.warn("unknown charset {} in hg config, fallback to utf-8", charset);
      return StandardCharsets.UTF_8;
    }
  }
}
