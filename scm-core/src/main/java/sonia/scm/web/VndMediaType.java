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

package sonia.scm.web;

import jakarta.ws.rs.core.MediaType;

/**
 * Vendor media types used by SCMM.
 */
public class VndMediaType {

  private static final String VERSION = "2";
  private static final String TYPE = "application";
  private static final String SUBTYPE_PREFIX = "vnd.scmm-";
  public static final String PREFIX = TYPE + "/" + SUBTYPE_PREFIX;
  public static final String SUFFIX = "+json;v=" + VERSION;
  public static final String PLAIN_TEXT_PREFIX = "text/" + SUBTYPE_PREFIX;
  public static final String PLAIN_TEXT_SUFFIX = "+plain;v=" + VERSION;

  public static final String INDEX = PREFIX + "index" + SUFFIX;
  public static final String USER = PREFIX + "user" + SUFFIX;
  public static final String GROUP = PREFIX + "group" + SUFFIX;
  public static final String AUTOCOMPLETE =  PREFIX + "autocomplete" + SUFFIX;
  public static final String REPOSITORY = PREFIX + "repository" + SUFFIX;
  public static final String NAMESPACE = PREFIX + "namespace" + SUFFIX;
  public static final String REPOSITORY_PERMISSION = PREFIX + "repositoryPermission" + SUFFIX;
  public static final String CHANGESET = PREFIX + "changeset" + SUFFIX;
  public static final String CHANGESET_COLLECTION = PREFIX + "changesetCollection" + SUFFIX;
  public static final String REVERT = PREFIX + "revert" + SUFFIX;
  public static final String MODIFICATIONS = PREFIX + "modifications" + SUFFIX;
  public static final String TAG = PREFIX + "tag" + SUFFIX;
  public static final String TAG_COLLECTION = PREFIX + "tagCollection" + SUFFIX;
  public static final String TAG_REQUEST = PREFIX + "tagRequest" + SUFFIX;
  public static final String BRANCH = PREFIX + "branch" + SUFFIX;
  public static final String BRANCH_DETAILS = PREFIX + "branchDetails" + SUFFIX;
  public static final String BRANCH_DETAILS_COLLECTION = PREFIX + "branchDetailsCollection" + SUFFIX;
  public static final String BRANCH_REQUEST = PREFIX + "branchRequest" + SUFFIX;
  public static final String DIFF = PLAIN_TEXT_PREFIX + "diff" + PLAIN_TEXT_SUFFIX;
  public static final String DIFF_PARSED = PREFIX + "diffParsed" + SUFFIX;
  public static final String USER_COLLECTION = PREFIX + "userCollection" + SUFFIX;
  public static final String GROUP_COLLECTION = PREFIX + "groupCollection" + SUFFIX;
  public static final String REPOSITORY_COLLECTION = PREFIX + "repositoryCollection" + SUFFIX;
  public static final String NAMESPACE_COLLECTION = PREFIX + "namespaceCollection" + SUFFIX;
  public static final String BRANCH_COLLECTION = PREFIX + "branchCollection" + SUFFIX;
  public static final String CONFIG = PREFIX + "config" + SUFFIX;
  public static final String REPOSITORY_VERB_COLLECTION = PREFIX + "repositoryVerbCollection" + SUFFIX;
  public static final String REPOSITORY_TYPE_COLLECTION = PREFIX + "repositoryTypeCollection" + SUFFIX;
  public static final String REPOSITORY_TYPE = PREFIX + "repositoryType" + SUFFIX;
  public static final String PLUGIN = PREFIX + "plugin" + SUFFIX;
  public static final String PLUGIN_COLLECTION = PREFIX + "pluginCollection" + SUFFIX;
  public static final String PLUGIN_CENTER_AUTH_INFO = PREFIX + "pluginCenterAuthInfo" + SUFFIX;
  public static final String UI_PLUGIN = PREFIX + "uiPlugin" + SUFFIX;
  public static final String UI_PLUGIN_COLLECTION = PREFIX + "uiPluginCollection" + SUFFIX;
  @SuppressWarnings("squid:S2068")
  public static final String PASSWORD_CHANGE = PREFIX + "passwordChange" + SUFFIX;
  @SuppressWarnings("squid:S2068")
  public static final String PASSWORD_OVERWRITE = PREFIX + "passwordOverwrite" + SUFFIX;
  public static final String PERMISSION_COLLECTION = PREFIX + "permissionCollection" + SUFFIX;

  public static final String NAMESPACE_STRATEGIES = PREFIX + "namespaceStrategies" + SUFFIX;

  public static final String ME = PREFIX + "me" + SUFFIX;
  public static final String SOURCE = PREFIX + "source" + SUFFIX;
  public static final String ANNOTATE = PREFIX + "annotate" + SUFFIX;
  public static final String REPOSITORY_PATHS = PREFIX + "repositoryPaths" + SUFFIX;
  public static final String ADMIN_INFO = PREFIX + "adminInfo" + SUFFIX;
  public static final String ERROR_TYPE = PREFIX + "error" + SUFFIX;

  public static final String REPOSITORY_ROLE = PREFIX + "repositoryRole" + SUFFIX;
  public static final String REPOSITORY_ROLE_COLLECTION = PREFIX + "repositoryRoleCollection" + SUFFIX;

  public static final String API_KEY = PREFIX + "apiKey" + SUFFIX;
  public static final String API_KEY_COLLECTION = PREFIX + "apiKeyCollection" + SUFFIX;

  public static final String REPOSITORY_EXPORT = PREFIX + "repositoryExport" + SUFFIX;
  public static final String REPOSITORY_EXPORT_INFO = PREFIX + "repositoryExportInfo" + SUFFIX;

  public static final String NOTIFICATION_COLLECTION = PREFIX + "notificationCollection" + SUFFIX;

  public static final String ALERTS_REQUEST = PREFIX + "alertsRequest" + SUFFIX;

  public static final String QUERY_RESULT = PREFIX + "queryResult" + SUFFIX;
  public static final String SEARCHABLE_TYPE_COLLECTION = PREFIX + "searchableTypeCollection" + SUFFIX;

  private VndMediaType() {
  }

  /**
   * Checks whether the given media type is a media type used by SCMM.
   */
  public static boolean isVndType(MediaType type) {
    return type.getType().equals(TYPE) && type.getSubtype().startsWith(SUBTYPE_PREFIX);
  }
}
