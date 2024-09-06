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

import { HalRepresentation, PagedCollection } from "./hal";

export type DisplayedUser = {
  id: string;
  displayName: string;
  mail?: string;
};

export type UserBase = {
  displayName: string;
  name: string;
  mail?: string;
  password: string;
  active: boolean;
  type?: string;
  creationDate?: string;
  lastModified?: string;
  external: boolean;
};

export type User = HalRepresentation & UserBase;
export type UserCreation = User;

export type UserCollection = PagedCollection<{
  users: User[];
}> & {
  externalAuthenticationAvailable: boolean;
};

export type PermissionOverview = HalRepresentation & {
  relevantGroups: PermissionOverviewGroupEntry[];
  relevantNamespaces: string[];
  relevantRepositories: PermissionOverviewRepositoryEntry[];
};

export type PermissionOverviewGroupEntry = {
  name: string;
  permissions: boolean;
  externalOnly: boolean;
};

export type PermissionOverviewRepositoryEntry = {
  namespace: string;
  name: string;
};
