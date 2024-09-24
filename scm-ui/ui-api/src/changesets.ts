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
import { useQuery, useQueryClient } from "react-query";
import { requiredLink } from "./links";
import { apiClient } from "./apiclient";
import { ApiResult, ApiResultWithFetching } from "./base";
import { branchQueryKey, repoQueryKey } from "./keys";
import { concat } from "./urls";

type UseChangesetsRequest = {
  branch?: Branch;
  page?: string | number;
  limit?: number;
};

export const changesetQueryKey = (repository: NamespaceAndName, id: string) => {
  return repoQueryKey(repository, "changeset", id);
};

export const useChangesets = (
  repository: Repository,
  request?: UseChangesetsRequest
): ApiResultWithFetching<ChangesetCollection> => {
  const queryClient = useQueryClient();

  let link: string;
  let branch = "_";
  if (request?.branch) {
    link = requiredLink(request.branch, "history");
    branch = request.branch.name;
  } else {
    link = requiredLink(repository, "changesets");
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

  const key = branchQueryKey(repository, branch, "changesets", request?.page || 0);
  return useQuery<ChangesetCollection, Error>(key, () => apiClient.get(link).then((response) => response.json()), {
    onSuccess: (changesetCollection) => {
      changesetCollection._embedded?.changesets.forEach((changeset) => {
        queryClient.setQueryData(changesetQueryKey(repository, changeset.id), changeset);
      });
    },
  });
};

export const useChangeset = (repository: Repository, id: string): ApiResult<Changeset> => {
  const changesetsLink = requiredLink(repository, "changesets");
  return useQuery<Changeset, Error>(changesetQueryKey(repository, id), () =>
    apiClient.get(concat(changesetsLink, id)).then((response) => response.json())
  );
};
