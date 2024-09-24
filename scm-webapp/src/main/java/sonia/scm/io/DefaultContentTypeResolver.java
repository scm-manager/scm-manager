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

package sonia.scm.io;

import com.cloudogu.spotter.ContentType;
import com.cloudogu.spotter.ContentTypeDetector;
import com.cloudogu.spotter.Language;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class DefaultContentTypeResolver implements ContentTypeResolver {

  private final Set<ContentTypeResolverExtension> resolverExtensions;

  private static final Language[] BOOST = new Language[]{
    // GCC Machine Description uses .md as extension, but markdown is much more likely
    Language.MARKDOWN,
    // XML uses .rs as extension, but rust is much more likely
    Language.RUST,
    // XML is also returned by content type boost strategy, but rust is really much more likely
    Language.RUST,
  };

  private static final ContentTypeDetector PATH_BASED = ContentTypeDetector.builder()
    .defaultPathBased()
    .boost(BOOST)
    .bestEffortMatch();

  private static final ContentTypeDetector PATH_AND_CONTENT_BASED = ContentTypeDetector.builder()
    .defaultPathAndContentBased()
    .boost(BOOST)
    .bestEffortMatch();

  @Inject
  public DefaultContentTypeResolver(Set<ContentTypeResolverExtension> resolverExtensions) {
    this.resolverExtensions = resolverExtensions;
  }

  @Override
  public DefaultContentType resolve(String path) {
    Optional<String> extensionContentType = resolveContentTypeFromExtensions(path, new byte[]{});
    return extensionContentType
      .map(rawContentType -> new DefaultContentType(new ContentType(rawContentType)))
      .orElseGet(() -> new DefaultContentType(PATH_BASED.detect(path)));
  }

  @Override
  public DefaultContentType resolve(String path, byte[] contentPrefix) {
    Optional<String> extensionContentType = resolveContentTypeFromExtensions(path, contentPrefix);
    return extensionContentType
      .map(rawContentType -> new DefaultContentType(new ContentType(rawContentType)))
      .orElseGet(() -> new DefaultContentType(PATH_AND_CONTENT_BASED.detect(path, contentPrefix)));
  }

  @Override
  public Map<String, String> findSyntaxModesByLanguage(String language) {
    Optional<Language> byName = Language.getByName(language);
    if (byName.isPresent()) {
      return DefaultContentType.syntaxMode(byName.get());
    }
    return Collections.emptyMap();
  }

  private Optional<String> resolveContentTypeFromExtensions(String path, byte[] contentPrefix) {
    return resolverExtensions.stream()
      .map(r -> r.resolve(path, contentPrefix))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findFirst();
  }
}
