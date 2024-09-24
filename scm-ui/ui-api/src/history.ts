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

import { ApiResult } from "./base";
import { Changeset, ChangesetCollection, File, Link, Repository } from "@scm-manager/ui-types";
import { useQuery, useQueryClient } from "react-query";
import { apiClient } from "./apiclient";
import { createQueryString } from "./utils";
import { changesetQueryKey } from "./changesets";
import { repoQueryKey } from "./keys";

export type UseHistoryRequest = {
  page?: number | string;
};

export const useHistory = (
  repository: Repository,
  revision: string,
  file: File,
  request?: UseHistoryRequest
): ApiResult<ChangesetCollection> => {
  const queryClient = useQueryClient();
  const link = (file._links.history as Link).href;

  const queryParams: Record<string, string> = {};
  if (request?.page) {
    queryParams.page = request.page.toString();
  }

  return useQuery<ChangesetCollection, Error>(
    repoQueryKey(repository, "history", revision, file.path, request?.page || 0),
    () => apiClient.get(`${link}?${createQueryString(queryParams)}`).then((response) => response.json()),
    {
      keepPreviousData: true,
      onSuccess: (changesets: ChangesetCollection) => {
        changesets._embedded?.changesets.forEach((changeset: Changeset) =>
          queryClient.setQueryData(changesetQueryKey(repository, changeset.id), changeset)
        );
      },
    }
  );
};
