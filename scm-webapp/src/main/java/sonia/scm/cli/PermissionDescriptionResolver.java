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

package sonia.scm.cli;

import com.fasterxml.jackson.databind.JsonNode;
import sonia.scm.i18n.I18nCollector;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class PermissionDescriptionResolver {

  private static final String DISPLAYNAME = "displayName";
  private final I18nCollector i18nCollector;
  private final Locale locale;

  PermissionDescriptionResolver(I18nCollector i18nCollector, Locale locale) {
    this.i18nCollector = i18nCollector;
    this.locale = locale;
  }

  public Optional<String> getDescription(String verb) {
    collectI18nJson();
    return getVerbDescriptionFromI18nBundle(verb);
  }

  public Optional<String> getGlobalDescription(String verb) {
    collectI18nJson();
    return getGlobalVerbDescriptionFromI18nBundle(verb);
  }

  private void collectI18nJson() {
    try {
      i18nCollector.findJson(locale.getLanguage());
    } catch (IOException e) {
      throw new RuntimeException("failed to load i18n package", e);
    }
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
    JsonNode displayNameNode = permissionNode.get(DISPLAYNAME);
    if (displayNameNode == null) {
      return empty();
    }

    return of(displayNameNode.asText());
  }

  private Optional<String> getGlobalVerbDescriptionFromI18nBundle(String verb) {
    String[] verbParts = verb.split(":");

    Optional<JsonNode> jsonNode;
    try {
      jsonNode = i18nCollector.findJson(locale.getLanguage());
    } catch (IOException e) {
      return empty();
    }
    if (jsonNode.isEmpty()) {
      return empty();
    }
    JsonNode permissionsNode = jsonNode.get().get("permissions");
    if (permissionsNode == null) {
      return empty();
    }
    if (verbParts.length == 1) {
      return resolveSinglePartPermission(verb, permissionsNode);
    } else if (verbParts.length == 2) {
      return resolveTwoPartPermission(verbParts, permissionsNode);
    } else if (verbParts.length == 3) {
      return resolveThreePartPermission(verbParts, permissionsNode);
    }

    return empty();
  }

  private Optional<String> resolveSinglePartPermission(String verb, JsonNode permissionsNode) {
    JsonNode firstNode = permissionsNode.get(verb);
    if (firstNode == null) {
      return empty();
    }
    JsonNode displayNameNode = firstNode.get(DISPLAYNAME);
    if (displayNameNode == null) {
      return empty();
    }
    return of(displayNameNode.asText());
  }

  private Optional<String> resolveTwoPartPermission(String[] verbParts, JsonNode permissionsNode) {
    JsonNode firstNode = permissionsNode.get(verbParts[0]);
    if (firstNode == null) {
      return empty();
    }
    JsonNode secondNode = firstNode.get(verbParts[1]);
    if (secondNode == null) {
      return empty();
    }
    JsonNode displayNameNode = secondNode.get(DISPLAYNAME);
    if (displayNameNode == null) {
      return empty();
    }
    return of(displayNameNode.asText());
  }

  private Optional<String> resolveThreePartPermission(String[] verbParts, JsonNode permissionsNode) {
    JsonNode firstNode = permissionsNode.get(verbParts[0]);
    if (firstNode == null) {
      return empty();
    }
    JsonNode secondNode = firstNode.get(verbParts[1]);
    if (secondNode == null) {
      return empty();
    }
    JsonNode thirdNode = secondNode.get(verbParts[2]);
    if (thirdNode == null) {
      return empty();
    }
    JsonNode displayNameNode = thirdNode.get(DISPLAYNAME);
    if (displayNameNode == null) {
      return empty();
    }
    return of(displayNameNode.asText());
  }
}
