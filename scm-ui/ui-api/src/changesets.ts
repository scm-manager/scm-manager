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

import { Branch, Changeset, ChangesetCollection, NamespaceAndName, Repository } from "@scm-manager/ui-types";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { requiredLink } from "./links";
import { apiClient } from "./apiclient";
import { ApiResult, ApiResultWithFetching } from "./base";
import { branchQueryKey, repoQueryKey } from "./keys";
import { concat } from "./urls";
import { useEffect } from "react";

type UseChangesetsRequest = {
  revision?: string;
  branch?: Branch;
  page?: string | number;
  limit?: number;
  messageFilter?: string;
  personFilter?: string;
  fromFilter?: Date;
  query?: string;
};

export const changesetQueryKey = (repository: NamespaceAndName, id: string) => {
  return repoQueryKey(repository, "changeset", id);
};

export const useChangesets = (
  repository: Repository,
  request?: UseChangesetsRequest,
): ApiResultWithFetching<ChangesetCollection> => {
  const queryClient = useQueryClient();

  let link: string;
  let revision = "_";
  const appendFilter = (f: string, p: string, v: string) => {
    if (f) {
      return `${f}&${p}=${v}`;
    } else {
      return `${p}=${v}`;
    }
  };
  let filter = "";

  if (request?.branch) {
    link = requiredLink(request.branch, "history");
    revision = request.branch.name;
  } else {
    link = requiredLink(repository, "changesets");
    if (request?.revision) {
      filter = appendFilter(filter, "revision", request.revision);
      revision = request.revision;
    }
  }

  if (request?.page || request?.limit) {
    if (request?.page && request?.limit) {
      link = `${link}?page=${request.page}&pageSize=${request.limit}`;
    } else if (request.page) {
      link = `${link}?page=${request.page}`;
    } else if (request.limit) {
      link = `${link}?pageSize=${request.limit}`;
    }
  }
  if (request?.messageFilter) {
    filter = appendFilter(filter, "message", request.messageFilter);
  }
  if (request?.personFilter) {
    filter = appendFilter(filter, "person", request.personFilter);
  }
  if (request?.fromFilter) {
    filter = appendFilter(filter, "from", request.fromFilter.toISOString());
  }
  const isSearchMode = request?.query !== undefined;
  const isEnabled = isSearchMode ? request.query!.length >= 3 : true;

  if (isSearchMode && request.query!.length >= 3) {
    filter = appendFilter(filter, "q", request.query!);
  }
  if (filter) {
    if (link.indexOf("?") > 0) {
      link = link + "&" + filter;
    } else {
      link = link + "?" + filter;
    }
  }
  const key = branchQueryKey(repository, revision, "changesets", request?.page || 0);
  key.push(filter || "");

  const result = useQuery<ChangesetCollection, Error>({
    queryKey: key,
    queryFn: () => apiClient.get(link).then((response) => response.json()),
    enabled: isEnabled,
  });
  useEffect(() => {
    if (result.data) {
      result.data._embedded?.changesets.forEach((changeset) => {
        queryClient.setQueryData(changesetQueryKey(repository, changeset.id), changeset);
      });
    }
  }, [queryClient, repository, result.data]);
  return result;
};

export const useChangeset = (repository: Repository, id: string): ApiResult<Changeset> => {
  const changesetsLink = requiredLink(repository, "changesets");
  return useQuery<Changeset, Error>({
    queryKey: changesetQueryKey(repository, id),
    queryFn: () => apiClient.get(concat(changesetsLink, id)).then((response) => response.json()),
  });
};
