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

package sonia.scm.repository.api;


import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Feature;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKey;
import sonia.scm.repository.Tag;
import sonia.scm.repository.Tags;
import sonia.scm.repository.spi.TagsCommand;

import java.io.IOException;
import java.util.List;

/**
 * The tags command list all repository tags.<br />
 * <br />
 * <b>Samples:</b>
 * <br />
 * <br />
 * Return all tags of a repository:<br />
 * <pre><code>
 * TagsCommandBuilder tagsCommand = repositoryService.getTagsCommand();
 * Tags tags = tagsCommand.getTags();
 * </code></pre>
 *
 * @since 1.18
 */
public final class TagsCommandBuilder {

  static final String CACHE_NAME = "sonia.cache.cmd.tags";


  private static final Logger logger =
    LoggerFactory.getLogger(TagsCommandBuilder.class);

  /**
   * cache for changesets
   */
  private final Cache<CacheKey, Tags> cache;

  private final TagsCommand command;

  private final Repository repository;

  private boolean disableCache = false;

  private String revision;

  /**
   * Constructs a new {@link TagsCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param cacheManager cache manager
   * @param command      implementation of the {@link TagsCommand}
   * @param repository   repository
   */
  TagsCommandBuilder(CacheManager cacheManager, TagsCommand command,
                     Repository repository) {
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.command = command;
    this.repository = repository;
  }


  /**
   * Returns all tags from the repository.
   */
  public Tags getTags() throws IOException {
    if (revision != null) {
      logger.debug("get tags for repository {} with revision {}", repository, revision);
      return getTagsFromCommandForRevision();
    } else if (disableCache) {
      logger.debug("get tags for repository {} with disabled cache", repository);
      return getTagsFromCommand();
    } else {
      CacheKey key = new CacheKey(repository);
      Tags tags = cache.get(key);
      if (tags == null) {
        logger.debug("get tags for repository {}", repository);
        tags = getTagsFromCommand();
        cache.put(key, tags);
      } else {
        logger.debug("get tags for repository {} from cache", repository);
      }
      return tags;
    }
  }

  /**
   * Disables the cache for tags. This means that every {@link Tag}
   * is directly retrieved from the {@link Repository}. <b>Note: </b> Disabling
   * the cache cost a lot of performance and could be much slower.
   *
   * @param disableCache true to disable the cache
   * @return {@code this}
   */
  public TagsCommandBuilder setDisableCache(boolean disableCache) {
    this.disableCache = disableCache;

    return this;
  }

  /**
   * Set revision to show all tags containing the given revision. This is only supported for repositories supporting
   * feature {@link sonia.scm.repository.Feature#TAGS_FOR_REVISION} (@see {@link RepositoryService#isSupported(Feature)}).
   *
   * @return {@code this}
   * @since 3.1.0
   */
  public TagsCommandBuilder forRevision(String revision) {
    this.revision = revision;

    return this;
  }

  private Tags getTagsFromCommand() throws IOException {
    List<Tag> tagList = command.getTags();

    return new Tags(tagList);
  }

  private Tags getTagsFromCommandForRevision() throws IOException {
    return new Tags(command.getTags(revision));
  }


  static class CacheKey implements RepositoryCacheKey {
    private final String repositoryId;

    public CacheKey(Repository repository) {
      this.repositoryId = repository.getId();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }

      if (getClass() != obj.getClass()) {
        return false;
      }

      final CacheKey other = (CacheKey) obj;

      return Objects.equal(repositoryId, other.repositoryId);
    }


    @Override
    public int hashCode() {
      return Objects.hashCode(repositoryId);
    }


    @Override
    public String getRepositoryId() {
      return repositoryId;
    }

  }

}
