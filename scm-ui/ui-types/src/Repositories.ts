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

import { HalRepresentation, Links, PagedCollection } from "./hal";

export type NamespaceAndName = {
  namespace: string;
  name: string;
};

export type HealthCheckFailure = {
  id: string;
  description: string;
  summary: string;
  url: string;
};

export type RepositoryBase = NamespaceAndName & {
  type: string;
  contact?: string;
  description?: string;
};

export type Repository = HalRepresentation &
  RepositoryBase & {
    creationDate?: string;
    lastModified?: string;
    archived?: boolean;
    exporting?: boolean;
    healthCheckFailures?: HealthCheckFailure[];
    healthCheckRunning?: boolean;
  };

export type RepositoryCreation = RepositoryBase & {
  contextEntries?: { [key: string]: object | undefined };
};

export type RepositoryUrlImport = RepositoryCreation & {
  importUrl: string;
  username?: string;
  password?: string;
  skipLfs?: boolean;
};

export type ExportInfo = HalRepresentation & {
  exporterName: string;
  created: Date;
  withMetadata: boolean;
  compressed: boolean;
  encrypted: boolean;
  status: "FINISHED" | "INTERRUPTED" | "EXPORTING";
};

export type Namespace = {
  namespace: string;
  _links: Links;
};

type RepositoryEmbedded = {
  repositories: Repository[];
};

export type RepositoryCollection = PagedCollection<RepositoryEmbedded>;

export type NamespaceCollection = {
  _embedded: {
    namespaces: Namespace[];
  };
};

export type RepositoryGroup = {
  name: string;
  namespace?: Namespace;
  repositories: Repository[];
};
