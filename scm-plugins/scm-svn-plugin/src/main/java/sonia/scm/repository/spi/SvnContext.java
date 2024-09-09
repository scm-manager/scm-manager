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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.repository.SvnUtil;

import java.io.Closeable;
import java.io.File;


public class SvnContext implements Closeable, RepositoryProvider {

  private static final Logger LOG = LoggerFactory.getLogger(SvnContext.class);

  private final Repository repository;
  private final File directory;

  private SVNRepository svnRepository;

  public SvnContext(Repository repository, File directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public Repository getRepository() {
    return repository;
  }

  @Override
  public Repository get() {
    return getRepository();
  }

  public File getDirectory() {
    return directory;
  }

  public SVNURL createUrl() throws SVNException {
    return SVNURL.fromFile(directory);
  }

  public SVNRepository open() throws SVNException {
    if (svnRepository == null) {
      LOG.trace("open svn repository {}", directory);
      svnRepository = SVNRepositoryFactory.create(createUrl());
    }

    return svnRepository;
  }

  @Override
  public void close() {
    LOG.trace("close svn repository {}", directory);
    SvnUtil.closeSession(svnRepository);
  }

}
