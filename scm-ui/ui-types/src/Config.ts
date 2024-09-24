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

import { HalRepresentation } from "./hal";

export type AnonymousMode = "FULL" | "PROTOCOL_ONLY" | "OFF";

export type Config = HalRepresentation & {
  proxyPassword: string | null;
  proxyPort: number;
  proxyServer: string;
  proxyUser: string | null;
  enableProxy: boolean;
  realmDescription: string;
  disableGroupingGrid: boolean;
  dateFormat: string;
  anonymousAccessEnabled: boolean;
  anonymousMode: AnonymousMode;
  baseUrl: string;
  forceBaseUrl: boolean;
  loginAttemptLimit: number;
  proxyExcludes: string[];
  skipFailedAuthenticators: boolean;
  pluginUrl: string;
  pluginAuthUrl: string;
  loginAttemptLimitTimeout: number;
  enabledXsrfProtection: boolean;
  enabledUserConverter: boolean;
  namespaceStrategy: string;
  loginInfoUrl: string;
  alertsUrl: string;
  releaseFeedUrl: string;
  mailDomainName: string;
  emergencyContacts: string[];
  enabledApiKeys: boolean;
  enabledFileSearch: boolean;
};
