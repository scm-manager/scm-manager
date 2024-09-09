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

package sonia.scm.repository.spi;


import org.javahg.Repository;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgRepositoryFactory;
import sonia.scm.repository.RepositoryProvider;

import java.io.Closeable;
import java.io.File;


public class HgCommandContext implements Closeable, RepositoryProvider {

  private final HgConfigResolver configResolver;
  private final HgRepositoryFactory factory;
  private final sonia.scm.repository.Repository scmRepository;

  private Repository repository;

  public HgCommandContext(HgConfigResolver configResolver, HgRepositoryFactory factory, sonia.scm.repository.Repository scmRepository) {
    this.configResolver = configResolver;
    this.factory = factory;
    this.scmRepository = scmRepository;
  }

  public Repository open() {
    if (repository == null) {
      repository = factory.openForRead(scmRepository);
    }
    return repository;
  }

  public Repository openForWrite() {
    return factory.openForWrite(scmRepository);
  }

  private HgConfig config;

  public HgConfig getConfig() {
    if (config == null) {
      config = configResolver.resolve(scmRepository);
    }
    return config;
  }

  public File getDirectory() {
    return getConfig().getDirectory();
  }

  public sonia.scm.repository.Repository getScmRepository() {
    return scmRepository;
  }

  @Override
  public sonia.scm.repository.Repository get() {
    return getScmRepository();
  }

  @Override
  public void close() {
    if (repository != null) {
      repository.close();
    }
  }

}
