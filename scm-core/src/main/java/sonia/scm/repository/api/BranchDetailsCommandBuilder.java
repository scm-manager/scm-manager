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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKey;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.spi.BranchDetailsCommand;
import sonia.scm.repository.spi.BranchDetailsCommandRequest;

import java.io.Serializable;

/**
 * @since 2.28.0
 */
public final class BranchDetailsCommandBuilder {

  static final String CACHE_NAME = "sonia.cache.cmd.branch-details";
  private static final Logger LOG = LoggerFactory.getLogger(BranchDetailsCommandBuilder.class);

  private final Repository repository;
  private final BranchDetailsCommand command;
  private final Cache<CacheKey, BranchDetailsCommandResult> cache;

  public BranchDetailsCommandBuilder(Repository repository, BranchDetailsCommand command, CacheManager cacheManager) {
    this.repository = repository;
    this.command = command;
    this.cache = cacheManager.getCache(CACHE_NAME);
  }

  /**
   * Computes the details for the given branch.
   *
   * @param branchName Tha name of the branch the details should be computed for.
   * @return The result object containing the details for the branch.
   */
  @SuppressWarnings("javasecurity:S5145") // We validate branch names in the rest layer
  public BranchDetailsCommandResult execute(String branchName) {
    LOG.debug("get branch details for repository {} and branch {}", repository, branchName);
    RepositoryPermissions.read(repository).check();
    BranchDetailsCommandRequest branchDetailsCommandRequest = new BranchDetailsCommandRequest();
    branchDetailsCommandRequest.setBranchName(branchName);
    BranchDetailsCommandResult cachedResult = cache.get(createCacheKey(branchName));
    if (cachedResult != null) {
      LOG.debug("got result from cache for repository {} and branch {}", repository, branchName);
      return cachedResult;
    }

    BranchDetailsCommandResult result = command.execute(branchDetailsCommandRequest);
    cache.put(createCacheKey(branchName), result);
    return result;
  }

  private CacheKey createCacheKey(String branchName) {
    return new CacheKey(repository, branchName);
  }

  @AllArgsConstructor
  @Getter
  @EqualsAndHashCode
  static class CacheKey implements RepositoryCacheKey, Serializable {
    private Repository repository;
    private String branchName;

    @Override
    public String getRepositoryId() {
      return repository.getId();
    }
  }
}
