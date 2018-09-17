package sonia.scm.web.protocol;

import sonia.scm.repository.NamespaceAndName;
import sonia.scm.util.HttpUtil;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

final class NamespaceAndNameFromPathExtractor {

  private NamespaceAndNameFromPathExtractor() {}

  static Optional<NamespaceAndName> fromUri(String uri) {
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

    int nameDotIndex = name.indexOf('.');
    if (nameDotIndex >= 0) {
      return of(new NamespaceAndName(namespace, name.substring(0, nameDotIndex)));
    } else {
      return of(new NamespaceAndName(namespace, name));
    }
  }
}
