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
