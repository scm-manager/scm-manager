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
 *
 */

import { Branch, ChangesetCollection, Repository } from "@scm-manager/ui-types";
import { useQuery, useQueryClient } from "react-query";
import { requiredLink } from "./links";
import { apiClient } from "@scm-manager/ui-components";
import { ApiResult } from "./base";
import { branchQueryKey, repoQueryKey } from "./keys";

type UseChangesetsRequest = {
  branch?: Branch;
  page?: string | number;
};

export const useChangesets = (
  repository: Repository,
  request?: UseChangesetsRequest
): ApiResult<ChangesetCollection> => {
  const queryClient = useQueryClient();

  let link: string;
  let branch = "_";
  if (request?.branch) {
    link = requiredLink(request.branch, "history");
    branch = request.branch.name;
  } else {
    link = requiredLink(repository, "changesets");
  }

  if (request?.page) {
    link = `${link}?page=${request.page}`;
  }

  const key = branchQueryKey(repository, branch, "changesets", request?.page || 0);
  return useQuery<ChangesetCollection, Error>(key, () => apiClient.get(link).then(response => response.json()), {
    onSuccess: changesetCollection => {
      changesetCollection._embedded.changesets.forEach(changeset => {
        queryClient.setQueryData(repoQueryKey(repository, "changeset", changeset.id), changeset);
      });
    }
  });
};
