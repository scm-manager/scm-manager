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

package sonia.scm.repository.api;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKey;
import sonia.scm.repository.spi.ChangesetsCommand;
import sonia.scm.repository.spi.ChangesetsCommandRequest;

import java.io.Serializable;
import java.util.Optional;

public class ChangesetsCommandBuilder {

  static final String CACHE_NAME = "sonia.cache.cmd.changesets";
  private final Cache<CacheKey, Iterable<Changeset>> cache;
  private static final Logger LOG = LoggerFactory.getLogger(ChangesetsCommandBuilder.class);

  private final Repository repository;
  private final ChangesetsCommand changesetsCommand;

  private final ChangesetsCommandRequest request = new ChangesetsCommandRequest();

  public ChangesetsCommandBuilder(CacheManager cacheManager, Repository repository, ChangesetsCommand changesetsCommand) {
    this.repository = repository;
    this.changesetsCommand = changesetsCommand;
    this.cache = cacheManager.getCache(CACHE_NAME);
  }

  public Iterable<Changeset> getChangesets() {
    CacheKey cacheKey = new CacheKey(repository);
    Iterable<Changeset> changesets = cache.get(cacheKey);

    if (changesets == null || Iterables.isEmpty(changesets)) {
      LOG.debug("Retrieve all changesets from {{}}", repository);
      changesets = changesetsCommand.getChangesets(request);
      cache.put(cacheKey, changesets);
    } else {
      LOG.debug("Use cached changesets from {{}}", repository);
    }

    return changesets;
  }

  public Optional<Changeset> getLatestChangeset() {
    LOG.debug("Retrieve latest changeset from {{}}", repository);
    return changesetsCommand.getLatestChangeset();
  }

  static class CacheKey implements RepositoryCacheKey, Serializable {
    private final Repository repository;

    CacheKey(Repository repository) {
      this.repository = repository;
    }

    @Override
    public String getRepositoryId() {
      return repository.getId();
    }
  }

}
