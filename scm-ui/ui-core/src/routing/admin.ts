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

export const ADMIN_BASE_ROUTE = "admin";

export const ADMIN_SUB_ROUTES = {
  INFO: "info",
  PLUGINS: "plugins",
  ROLES: "roles",
  ROLE: "role",
  SETTINGS: "settings",
} as const;

export const ADMIN_SETTINGS_SUB_ROUTES = {
  GENERAL: "general",
} as const;

export const ADMIN_PLUGINS_SUB_ROUTES = {
  INSTALLED: "installed",
  AVAILABLE: "available",
} as const;

export const ADMIN_ROLE_COLLECTION_SUB_ROUTES = {
  CREATE: "create",
} as const;

export const ADMIN_SINGLE_ROLE_SUB_ROUTES = {
  INFO: "info",
  EDIT: "edit",
} as const;
