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
    
package sonia.scm.web.protocol;

import jakarta.inject.Inject;
import sonia.scm.Type;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.util.HttpUtil;

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
