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
    () => apiClient.get(link).then((response) => response.json()),
    {
      onSuccess: (changesetCollection) => {
        changesetCollection._embedded?.changesets.forEach((changeset) => {
          queryClient.setQueryData(changesetQueryKey(repository, changeset.id), changeset);
        });
      },
    }
  );
};
