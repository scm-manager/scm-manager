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

package sonia.scm.api.v2.resources;

import sonia.scm.security.AnonymousMode;

import java.util.Set;

interface UpdateConfigDto {
  String getProxyPassword();

  int getProxyPort();

  String getProxyServer();

  String getProxyUser();

  boolean isEnableProxy();

  String getRealmDescription();

  boolean isDisableGroupingGrid();

  String getDateFormat();

  boolean isAnonymousAccessEnabled();

  AnonymousMode getAnonymousMode();

  String getBaseUrl();

  boolean isForceBaseUrl();

  int getLoginAttemptLimit();

  Set<String> getProxyExcludes();

  boolean isSkipFailedAuthenticators();

  String getPluginUrl();

  long getLoginAttemptLimitTimeout();

  boolean isEnabledXsrfProtection();

  boolean isEnabledUserConverter();

  String getNamespaceStrategy();

  String getLoginInfoUrl();

  /**
   * Get the url to the alerts api.
   *
   * @return alerts url
   * @since 2.30.0
   */
  String getAlertsUrl();

  String getReleaseFeedUrl();

  String getMailDomainName();

  Set<String> getEmergencyContacts();
}
