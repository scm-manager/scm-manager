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
