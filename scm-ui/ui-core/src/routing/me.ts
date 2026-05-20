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

export const ME_BASE_ROUTE = "me";

export const ME_SUB_ROUTES = {
  INFO: "info",
  SETTINGS: "settings",
} as const;

export const ME_SETTINGS_SUB_ROUTES = {
  THEME: "theme",
  ACCESSIBILITY: "accessibility",
  CODE: "code",
  PASSWORD: "password", // NOSONAR - S2068 false positive: used as route identifier, not a credential
  PUBLIC_KEYS: "publicKeys",
  API_KEYS: "apiKeys",
} as const;
