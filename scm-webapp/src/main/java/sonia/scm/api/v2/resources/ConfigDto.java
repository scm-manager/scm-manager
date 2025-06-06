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

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.security.AnonymousMode;

import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class ConfigDto extends HalRepresentation implements UpdateConfigDto {

  private String proxyPassword;
  private int proxyPort;
  private String proxyServer;
  private String proxyUser;
  private boolean enableProxy;
  private String realmDescription;
  private boolean disableGroupingGrid;
  private String dateFormat;
  private boolean anonymousAccessEnabled;
  private AnonymousMode anonymousMode;
  private String baseUrl;
  private boolean forceBaseUrl;
  private int loginAttemptLimit;
  private Set<String> proxyExcludes;
  private boolean skipFailedAuthenticators;
  private String pluginUrl;
  private String pluginAuthUrl;
  private long loginAttemptLimitTimeout;
  private boolean enabledXsrfProtection;
  private boolean enabledUserConverter;
  private boolean enabledApiKeys;
  private boolean enabledFileSearch;
  private String namespaceStrategy;
  private String loginInfoUrl;
  private String alertsUrl;
  private String releaseFeedUrl;
  private String mailDomainName;
  private int jwtExpirationInH;
  private boolean enabledJwtEndless;
  private Set<String> emergencyContacts;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
