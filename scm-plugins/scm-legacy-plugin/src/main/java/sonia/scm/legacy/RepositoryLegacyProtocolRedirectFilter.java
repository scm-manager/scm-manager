package sonia.scm.legacy;

import sonia.scm.migration.MigrationDAO;
import sonia.scm.migration.MigrationInfo;
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

@Singleton
public class RepositoryLegacyProtocolRedirectFilter extends HttpFilter {

  private final ProtocolBasedLegacyRepositoryInfo info;

  @Inject
  public RepositoryLegacyProtocolRedirectFilter(MigrationDAO migrationDAO) {
    this.info = load(migrationDAO);
  }

  private static ProtocolBasedLegacyRepositoryInfo load(MigrationDAO migrationDAO) {
    return new ProtocolBasedLegacyRepositoryInfo(migrationDAO.getAll());
  }

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    String servletPath = request.getServletPath();
    if (servletPath.startsWith("/")) {
      servletPath = servletPath.substring(1);
    }
    String[] pathElements = servletPath.split("/");
    if (pathElements.length > 0) {
      Optional<MigrationInfo> repository = info.findRepository(asList(pathElements));
      if (repository.isPresent()) {
        String furtherPath = servletPath.substring(repository.get().getProtocol().length() + 1 + repository.get().getOriginalRepositoryName().length());
        String queryString = request.getQueryString();
        if (queryString != null && queryString.length() > 0) {
          response.sendRedirect(String.format("%s/repo/%s/%s%s?%s", request.getContextPath(), repository.get().getNamespace(), repository.get().getName(), furtherPath, queryString));
        } else {
          response.sendRedirect(String.format("%s/repo/%s/%s%s", request.getContextPath(), repository.get().getNamespace(), repository.get().getName(), furtherPath));
        }
      } else {
        chain.doFilter(request, response);
      }
    } else {
      chain.doFilter(request, response);
    }
  }

  private static class ProtocolBasedLegacyRepositoryInfo {

    private final Map<String, LegacyRepositoryInfoCollection> infosForProtocol = new HashMap<>();

    public ProtocolBasedLegacyRepositoryInfo(Collection<MigrationInfo> all) {
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

    public boolean isProtocol(String protocol) {
      return infosForProtocol.containsKey(protocol);
    }
  }

  private static class LegacyRepositoryInfoCollection {

    private final Map<String, MigrationInfo> repositories = new HashMap<>();
    private final Map<String, LegacyRepositoryInfoCollection> next = new HashMap<>();

    public Optional<MigrationInfo> findRepository(List<String> pathElements) {
      if (repositories.containsKey(pathElements.get(0))) {
        return of(repositories.get(pathElements.get(0)));
      } else if (next.containsKey(pathElements.get(0))) {
        return next.get(pathElements.get(0)).findRepository(removeFirstElement(pathElements));
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
