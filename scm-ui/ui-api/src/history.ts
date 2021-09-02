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
