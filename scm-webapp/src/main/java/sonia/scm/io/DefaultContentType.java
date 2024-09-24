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

import com.cloudogu.spotter.Language;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class DefaultContentType implements ContentType {

  private final com.cloudogu.spotter.ContentType contentType;

  DefaultContentType(com.cloudogu.spotter.ContentType contentType) {
    this.contentType = contentType;
  }

  @Override
  public String getPrimary() {
    return contentType.getPrimary();
  }

  @Override
  public String getSecondary() {
    return contentType.getSecondary();
  }

  @Override
  public String getRaw() {
    return contentType.getRaw();
  }

  @Override
  public boolean isText() {
    return contentType.isText();
  }

  @Override
  public Optional<String> getLanguage() {
    return contentType.getLanguage().map(Language::getName);
  }

  @Override
  public Map<String, String> getSyntaxModes() {
    Optional<Language> language = contentType.getLanguage();
    if (language.isPresent()) {
      return syntaxMode(language.get());
    }
    return Collections.emptyMap();
  }

  static Map<String, String> syntaxMode(Language language) {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    language.getAceMode().ifPresent(mode -> builder.put("ace", mode));
    language.getCodemirrorMode().ifPresent(mode -> builder.put("codemirror", mode));
    language.getPrismMode().ifPresent(mode -> builder.put("prism", mode));
    return builder.build();
  }
}
