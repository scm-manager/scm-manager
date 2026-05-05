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

import { useLocation, useParams } from "react-router-dom";
import { useRepository } from "@scm-manager/ui-api";
import { Repository } from "@scm-manager/ui-types";

export const REPOSITORY_COLLECTION_BASE_ROUTE = "repos";
export const SINGLE_REPOSITORY_BASE_ROUTE = "repo";

export const REPOSITORY_COLLECTION_SUB_ROUTES = {
  IMPORT: "import",
  CREATE: "create",
} as const;

export const REPOSITORY_SUB_ROUTES = {
  INFO: "info",
  CODE: "code",
  CHANGES: "changes",
  BRANCHES: "branches",
  BRANCH: "branch",
  TAGS: "tags",
  TAG: "tag",
  REVISION: "revision",
  COMPARE: "compare",
  SEARCH: "search",
  SETTINGS: "settings",
  SOURCEEXT: "sourceext",
} as const;

export const REPOSITORY_CODE_SUB_ROUTES = {
  SOURCES: "sources",
  CHANGESETS: "changesets",
  CHANGESET: "changeset",
} as const;

export const REPOSITORY_BRANCHES_SUB_ROUTES = {
  CREATE: "create",
} as const;

export const REPOSITORY_SETTINGS_SUB_ROUTES = {
  GENERAL: "general",
  PERMISSIONS: "permissions",
} as const;

export const useRepositoryFromParams = () => {
  const { namespace, name } = useParams<{ namespace: string; name: string }>();
  if (!namespace || !name) {
    throw new Error("No namespace or name in url");
  }
  const { data: repository } = useRepository(namespace, name);
  if (!repository) {
    throw new Error(`No repository found for namespace ${namespace} and name ${name}`);
  }
  return repository;
};

type UrlParams = {
  sourceName: string;
  sourceType: string;
};

export const useSourceNameFromParams = () => {
  const params = useParams<UrlParams>();
  return params.sourceName;
};

export const useCurrentRepositoryBaseUrl = () => {
  const repository = useRepositoryFromParams();
  return getRepositoryBaseUrl(repository.namespace, repository.name);
};

export function getRepositoryBaseUrl(namespace: string, name: string): string;
export function getRepositoryBaseUrl(repository: Repository): string;
export function getRepositoryBaseUrl(arg1: string | Repository, arg2?: string): string {
  if (typeof arg1 === "string" && arg2) {
    return `/${SINGLE_REPOSITORY_BASE_ROUTE}/${arg1}/${arg2}`;
  } else {
    const repository = arg1 as Repository;
    return `/${SINGLE_REPOSITORY_BASE_ROUTE}/${repository.namespace}/${repository.name}`;
  }
}
