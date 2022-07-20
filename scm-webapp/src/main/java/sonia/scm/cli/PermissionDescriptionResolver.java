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

package sonia.scm.cli;

import com.fasterxml.jackson.databind.JsonNode;
import sonia.scm.i18n.I18nCollector;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class PermissionDescriptionResolver {
  private final I18nCollector i18nCollector;
  private final Locale locale;

  PermissionDescriptionResolver(I18nCollector i18nCollector, Locale locale) {
    this.i18nCollector = i18nCollector;
    this.locale = locale;
  }

  public Optional<String> getDescription(String verb) {
    try {
      i18nCollector.findJson(locale.getLanguage());
    } catch (IOException e) {
      throw new RuntimeException("failed to load i18n package", e);
    }
    return getVerbDescriptionFromI18nBundle(verb);
  }

  private Optional<String> getVerbDescriptionFromI18nBundle(String verb) {
    Optional<JsonNode> jsonNode;
    try {
      jsonNode = i18nCollector.findJson(locale.getLanguage());
    } catch (IOException e) {
      return empty();
    }
    if (jsonNode.isEmpty()) {
      return empty();
    }
    JsonNode verbsNode = jsonNode.get().get("verbs");
    if (verbsNode == null) {
      return empty();
    }
    JsonNode repositoryNode = verbsNode.get("repository");
    if (repositoryNode == null) {
      return empty();
    }
    JsonNode permissionNode = repositoryNode.get(verb);
    if (permissionNode == null) {
      return empty();
    }
    JsonNode displayNameNode = permissionNode.get("displayName");
    if (displayNameNode == null) {
      return empty();
    }

    return of(displayNameNode.asText());
  }
}
