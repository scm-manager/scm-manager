 /**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.repository;

 import com.google.common.collect.Maps;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sonia.scm.util.HttpUtil;
 import sonia.scm.util.Util;

 import javax.inject.Inject;
 import java.util.Map;
 import java.util.Set;

/**
 * RepositoryMatcher is able to check if a repository matches the requested path.
 * 
 * @author Sebastian Sdorra
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
