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

import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import java.util.Map;
import java.util.Set;

/**
 * RepositoryMatcher is able to check if a repository matches the requested path.
 * 
 * @since 1.54
 */
public final class RepositoryMatcher {
  
  private static final Logger LOG = LoggerFactory.getLogger(RepositoryMatcher.class);
  
  private static final RepositoryPathMatcher DEFAULT_PATH_MATCHER = new DefaultRepositoryPathMatcher();
  
  private final Map<String, RepositoryPathMatcher> pathMatchers;
  
  /**
   * Creates a new instance.
   * 
   * @param pathMatchers injected set of {@link RepositoryPathMatcher}.
   */
  @Inject
  public RepositoryMatcher(Set<RepositoryPathMatcher> pathMatchers) {
    this.pathMatchers = Maps.newHashMap();
    for ( RepositoryPathMatcher pathMatcher : pathMatchers ) {
      LOG.info("register custom repository path matcher for type {}", pathMatcher.getType());
      this.pathMatchers.put(pathMatcher.getType(), pathMatcher);
    }
  }
  
  /**
   * Returns {@code true} is the repository matches the type and the name matches the requested path.
   * 
   * @param repository repository
   * @param type type of repository
   * @param path requested path without context and without type information
   * 
   * @return {@code true} is the repository matches
   */
  public boolean matches(Repository repository, String type, String path) {
    return type.equals(repository.getType()) && isPathMatching(repository, path);
  }
  
  private boolean isPathMatching(Repository repository, String path) {

    String namespace = extractNamespace(path);
    String remainingPath = path.substring(namespace.length() + 1);

    return getPathMatcherForType(repository.getType()).isPathMatching(repository, remainingPath);
  }

  private String extractNamespace(String path) {
    if (path.startsWith(HttpUtil.SEPARATOR_PATH)) {
      path = path.substring(1);
    }
    int namespaceSeparator = path.indexOf(HttpUtil.SEPARATOR_PATH);
    if (namespaceSeparator > 0) {
      return path.substring(0, namespaceSeparator);
    }
    throw new IllegalArgumentException("no namespace in path " + path);
  }

  private RepositoryPathMatcher getPathMatcherForType(String type) {
    RepositoryPathMatcher pathMatcher = pathMatchers.get(type);
    if (pathMatcher == null) {
      pathMatcher = DEFAULT_PATH_MATCHER;
    }
    return pathMatcher;
  }
  
  private static class DefaultRepositoryPathMatcher implements RepositoryPathMatcher {

    @Override
    public boolean isPathMatching(Repository repository, String path) {
      String name = repository.getName();

      if (path.startsWith(name)) {
        String sub = path.substring(name.length());

        return Util.isEmpty(sub) || sub.startsWith(HttpUtil.SEPARATOR_PATH);
      }
    
      return false;
    }

    @Override
    public String getType() {
      return "any";
    }

  }
  
}
