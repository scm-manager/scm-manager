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

  public ModificationsCommandBuilder revision(String revision){
    request.setRevision(revision);
    return this;
  }

  /**
   * Reset each parameter to its default value.
   *
   *
   * @return {@code this}
   */
  public ModificationsCommandBuilder reset() {
    request.reset();
    this.disableCache = false;
    this.disablePreProcessors = false;
    return this;
  }

  public Modifications getModifications() throws IOException {
    Modifications modifications;
    if (disableCache) {
      log.info("Get modifications for {} with disabled cache", request);
      modifications = modificationsCommand.getModifications(request);
    } else {
      ModificationsCommandBuilder.CacheKey key = new ModificationsCommandBuilder.CacheKey(repository.getId(), request);
      if (cache.contains(key)) {
        modifications = cache.get(key);
        log.debug("Get modifications for {} from the cache", request);
      } else {
        log.info("Get modifications for {} with enabled cache", request);
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
