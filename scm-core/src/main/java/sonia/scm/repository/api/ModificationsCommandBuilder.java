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
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.cache.Cache;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKey;
import sonia.scm.repository.spi.ModificationsCommand;
import sonia.scm.repository.spi.ModificationsCommandRequest;

import java.io.IOException;

/**
 * Get the modifications applied to files in a revision.
 * <p>
 * Modifications are for example: Add, Update and Delete
 *
 * @author Mohamed Karray
 * @since 2.0
 */
@Slf4j
@RequiredArgsConstructor
@Accessors(fluent = true)
public final class ModificationsCommandBuilder {
  static final String CACHE_NAME = "sonia.cache.cmd.modifications";

  private final ModificationsCommand modificationsCommand;

  private final ModificationsCommandRequest request = new ModificationsCommandRequest();

  private final Repository repository;

  private final Cache<ModificationsCommandBuilder.CacheKey, Modifications> cache;

  private final PreProcessorUtil preProcessorUtil;

  @Setter
  private boolean disableCache = false;

  @Setter
  private boolean disablePreProcessors = false;

  /**
   * Set this to compute either the midifications of the given revision, or additionally set
   * {@link #baseRevision(String)} to compute the modifications between this and the
   * other revision.
   * @return This command builder.
   */
  public ModificationsCommandBuilder revision(String revision){
    request.setRevision(revision);
    return this;
  }

  /**
   * Set this to compute the modifications between two revisions. If this is not set,
   * only the modifications of the revision set by {@link #revision(String)} will be computed.
   * This is only supported by repositories supporting the feature
   * {@link sonia.scm.repository.Feature#MODIFICATIONS_BETWEEN_REVISIONS}.
   * @param baseRevision If set, the command will compute the modifications between this revision
   *                     and the revision set by {@link #revision(String)}.
   * @return This command builder.
   * @since 2.23.0
   */
  public ModificationsCommandBuilder baseRevision(String baseRevision){
    request.setBaseRevision(baseRevision);
    return this;
  }

  /**
   * Reset each parameter to its default value.
   *
   * @return {@code this}
   */
  public ModificationsCommandBuilder reset() {
    request.reset();
    this.disableCache = false;
    this.disablePreProcessors = false;
    return this;
  }

  /**
   * Computes the modifications.
   */
  public Modifications getModifications() throws IOException {
    Modifications modifications;
    if (disableCache) {
      log.debug("Get modifications for {} with disabled cache", request);
      modifications = modificationsCommand.getModifications(request);
    } else {
      ModificationsCommandBuilder.CacheKey key = new ModificationsCommandBuilder.CacheKey(repository.getId(), request);
      if (cache.contains(key)) {
        modifications = cache.get(key);
        log.debug("Get modifications for {} from the cache", request);
      } else {
        log.debug("Get modifications for {} with enabled cache", request);
        modifications = modificationsCommand.getModifications(request);
        if (modifications != null) {
          cache.put(key, modifications);
          log.debug("Modifications for {} added to the cache with key {}", request, key);
        }
      }
    }
    if (!disablePreProcessors && (modifications != null)) {
      preProcessorUtil.prepareForReturn(repository, modifications);
    }
    return modifications;
  }

  @AllArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  class CacheKey implements RepositoryCacheKey {
    private final String repositoryId;
    private final ModificationsCommandRequest request;

    @Override
    public String getRepositoryId() {
      return repositoryId;
    }
  }

}
