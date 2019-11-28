package sonia.scm.web.protocol;

import sonia.scm.Type;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.of;

final class NamespaceAndNameFromPathExtractor {

  private final Set<String> types;

  @Inject
  public NamespaceAndNameFromPathExtractor(RepositoryManager repositoryManager) {
    this.types = repositoryManager.getConfiguredTypes()
      .stream()
      .map(Type::getName)
      .collect(Collectors.toSet());
  }

  Optional<NamespaceAndName> fromUri(String uri) {
    if (uri.startsWith(HttpUtil.SEPARATOR_PATH)) {
      uri = uri.substring(1);
    }

    int endOfNamespace = uri.indexOf(HttpUtil.SEPARATOR_PATH);
    if (endOfNamespace < 1) {
      return empty();
    }

    String namespace = uri.substring(0, endOfNamespace);
    int nameSeparatorIndex = uri.indexOf(HttpUtil.SEPARATOR_PATH, endOfNamespace + 1);
    int nameIndex = nameSeparatorIndex > 0 ? nameSeparatorIndex : uri.length();
    if (nameIndex == endOfNamespace + 1) {
      return empty();
    }

    String name = uri.substring(endOfNamespace + 1, nameIndex);
    int nameDotIndex = name.lastIndexOf('.');
    if (nameDotIndex >= 0) {
      String suffix = name.substring(nameDotIndex + 1);
      if (types.contains(suffix)) {
        name = name.substring(0, nameDotIndex);
      }
    }
    return of(new NamespaceAndName(namespace, name));
  }
}
