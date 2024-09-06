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

import { ChangesetCollection, Link, Repository } from "@scm-manager/ui-types";
import { useQuery, useQueryClient } from "react-query";
import { ApiResultWithFetching } from "./base";
import { apiClient } from "./apiclient";
import { changesetQueryKey } from "./changesets";

function createIncomingUrl(repository: Repository, linkName: string, source: string, target: string) {
  const link = repository._links[linkName];
  if ((link as Link)?.templated) {
    return (link as Link).href
      .replace("{source}", encodeURIComponent(source))
      .replace("{target}", encodeURIComponent(target));
  } else {
    return (link as Link).href;
  }
}

export function createChangesetUrl(repository: Repository, source: string, target: string) {
  return createIncomingUrl(repository, "incomingChangesets", source, target);
}

export function createDiffUrl(repository: Repository, source: string, target: string) {
  if (repository._links.incomingDiffParsed) {
    return createIncomingUrl(repository, "incomingDiffParsed", source, target);
  } else {
    return createIncomingUrl(repository, "incomingDiff", source, target);
  }
}

type UseIncomingChangesetsRequest = {
  page?: string | number;
  limit?: number;
};

export const useIncomingChangesets = (
  repository: Repository,
  source: string,
  target: string,
  request?: UseIncomingChangesetsRequest
): ApiResultWithFetching<ChangesetCollection> => {
  const queryClient = useQueryClient();

  let link = createChangesetUrl(repository, source, target);

  if (request?.page || request?.limit) {
    if (request?.page && request?.limit) {
      link = `${link}?page=${request.page}&pageSize=${request.limit}`;
    } else if (request.page) {
      link = `${link}?page=${request.page}`;
    } else if (request.limit) {
      link = `${link}?pageSize=${request.limit}`;
    }
  }

  return useQuery<ChangesetCollection, Error>(
    ["repository", repository.namespace, repository.name, "compare", source, target, "changesets", request?.page || 0],
    () => apiClient.get(link).then(response => response.json()),
    {
      onSuccess: changesetCollection => {
        changesetCollection._embedded?.changesets.forEach(changeset => {
          queryClient.setQueryData(changesetQueryKey(repository, changeset.id), changeset);
        });
      }
    }
  );
};
