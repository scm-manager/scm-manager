/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
