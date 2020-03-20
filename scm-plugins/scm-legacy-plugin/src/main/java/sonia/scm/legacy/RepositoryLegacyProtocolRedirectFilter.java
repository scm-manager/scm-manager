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
    
package sonia.scm.legacy;

import sonia.scm.Priority;
import sonia.scm.filter.Filters;
import sonia.scm.migration.MigrationDAO;
import sonia.scm.migration.MigrationInfo;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.web.filter.HttpFilter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Priority(Filters.PRIORITY_BASEURL)
@Singleton
public class RepositoryLegacyProtocolRedirectFilter extends HttpFilter {

  private final ProtocolBasedLegacyRepositoryInfo info;
  private final RepositoryDAO repositoryDao;

  @Inject
  public RepositoryLegacyProtocolRedirectFilter(MigrationDAO migrationDAO, RepositoryDAO repositoryDao) {
    this.info = load(migrationDAO);
    this.repositoryDao = repositoryDao;
  }

  private static ProtocolBasedLegacyRepositoryInfo load(MigrationDAO migrationDAO) {
    return new ProtocolBasedLegacyRepositoryInfo(migrationDAO.getAll());
  }

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    new Worker(request, response, chain).doFilter();
  }

  private class Worker {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final FilterChain chain;

    private Worker(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
      this.request = request;
      this.response = response;
      this.chain = chain;
    }

    void doFilter() throws IOException, ServletException {
      String servletPath = getServletPathWithoutLeadingSlash();
      String[] pathElements = servletPath.split("/");
      if (pathElements.length > 0) {
        checkPathElements(servletPath, pathElements);
      } else {
        noRedirect();
      }
    }

    private void checkPathElements(String servletPath, String[] pathElements) throws IOException, ServletException {
      Optional<MigrationInfo> migrationInfo = info.findRepository(asList(pathElements));
      if (migrationInfo.isPresent()) {
        doRedirect(servletPath, migrationInfo.get());
      } else {
        noRedirect();
      }
    }

    private void doRedirect(String servletPath, MigrationInfo migrationInfo) throws IOException, ServletException {
      if (repositoryDao.get(migrationInfo.getId()) == null) {
        noRedirect();
      } else {
        String furtherPath = servletPath.substring(migrationInfo.getProtocol().length() + 1 + migrationInfo.getOriginalRepositoryName().length());
        String queryString = request.getQueryString();
        if (isEmpty(queryString)) {
          redirectWithoutQueryParameters(migrationInfo, furtherPath);
        } else {
          redirectWithQueryParameters(migrationInfo, furtherPath, queryString);
        }
      }
    }

    private void redirectWithoutQueryParameters(MigrationInfo migrationInfo, String furtherPath) throws IOException {
      response.sendRedirect(String.format("%s/repo/%s/%s%s", request.getContextPath(), migrationInfo.getNamespace(), migrationInfo.getName(), furtherPath));
    }

    private void redirectWithQueryParameters(MigrationInfo migrationInfo, String furtherPath, String queryString) throws IOException {
      response.sendRedirect(String.format("%s/repo/%s/%s%s?%s", request.getContextPath(), migrationInfo.getNamespace(), migrationInfo.getName(), furtherPath, queryString));
    }

    private void noRedirect() throws IOException, ServletException {
      chain.doFilter(request, response);
    }

    private String getServletPathWithoutLeadingSlash() {
      String servletPath = request.getServletPath();
      if (servletPath.startsWith("/")) {
        return servletPath.substring(1);
      } else {
        return servletPath;
      }
    }
  }

  private static class ProtocolBasedLegacyRepositoryInfo {

    private final Map<String, LegacyRepositoryInfoCollection> infosForProtocol = new HashMap<>();

    ProtocolBasedLegacyRepositoryInfo(Collection<MigrationInfo> all) {
      all.forEach(this::add);
    }

    private void add(MigrationInfo migrationInfo) {
      String protocol = migrationInfo.getProtocol();
      LegacyRepositoryInfoCollection legacyRepositoryInfoCollection = infosForProtocol.computeIfAbsent(protocol, x -> new LegacyRepositoryInfoCollection());
      legacyRepositoryInfoCollection.add(migrationInfo);
    }

    private Optional<MigrationInfo> findRepository(List<String> pathElements) {
      String protocol = pathElements.get(0);
      if (!isProtocol(protocol)) {
        return empty();
      }
      return infosForProtocol.get(protocol).findRepository(removeFirstElement(pathElements));
    }

    boolean isProtocol(String protocol) {
      return infosForProtocol.containsKey(protocol);
    }
  }

  private static class LegacyRepositoryInfoCollection {

    private final Map<String, MigrationInfo> repositories = new HashMap<>();
    private final Map<String, LegacyRepositoryInfoCollection> next = new HashMap<>();

    Optional<MigrationInfo> findRepository(List<String> pathElements) {
      String firstPathElement = pathElements.get(0);
      if (repositories.containsKey(firstPathElement)) {
        return of(repositories.get(firstPathElement));
      } else if (next.containsKey(firstPathElement)) {
        return next.get(firstPathElement).findRepository(removeFirstElement(pathElements));
      } else {
        return empty();
      }
    }

    private void add(MigrationInfo migrationInfo) {
      String originalRepositoryName = migrationInfo.getOriginalRepositoryName();
      List<String> originalRepositoryNameParts = asList(originalRepositoryName.split("/"));
      add(migrationInfo, originalRepositoryNameParts);
    }

    private void add(MigrationInfo migrationInfo, List<String> originalRepositoryNameParts) {
      if (originalRepositoryNameParts.isEmpty()) {
        throw new IllegalArgumentException("cannot handle empty name");
      } else if (originalRepositoryNameParts.size() == 1) {
        repositories.put(originalRepositoryNameParts.get(0), migrationInfo);
      } else {
        LegacyRepositoryInfoCollection subCollection = next.computeIfAbsent(originalRepositoryNameParts.get(0), x -> new LegacyRepositoryInfoCollection());
        subCollection.add(migrationInfo, removeFirstElement(originalRepositoryNameParts));
      }
    }
  }

  private static <T> List<T> removeFirstElement(List<T> originalRepositoryNameParts) {
    return originalRepositoryNameParts.subList(1, originalRepositoryNameParts.size());
  }
}
