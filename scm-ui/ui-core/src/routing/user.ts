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

export const USER_COLLECTION_BASE_ROUTE = "users";

export const USER_COLLECTION_SUB_ROUTES = {
  CREATE: "create",
} as const;

export const SINGLE_USER_BASE_ROUTE = "user";

export const SINGLE_USER_SUB_ROUTES = {
  INFO: "info",
  SETTINGS: "settings",
} as const;

export const SINGLE_USER_SETTINGS_SUB_ROUTES = {
  GENERAL: "general",
  PASSWORD: "password", // NOSONAR - S2068 false positive: used as route identifier, not a credential
  PERMISSIONS: "permissions",
  PUBLIC_KEYS: "publicKeys",
  API_KEYS: "apiKeys",
} as const;
