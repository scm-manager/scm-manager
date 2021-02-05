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

import { BranchCollection, Repository } from "@scm-manager/ui-types";
import { requiredLink } from "./links";
import { useQuery, useQueryClient } from "react-query";
import { ApiResult } from "./base";
import { branchQueryKey, repoQueryKey } from "./keys";
import { apiClient } from "@scm-manager/ui-components";

export const useBranches = (repository: Repository): ApiResult<BranchCollection> => {
  const queryClient = useQueryClient();
  const link = requiredLink(repository, "branches");
  return useQuery<BranchCollection, Error>(
    repoQueryKey(repository, "branches"),
    () => apiClient.get(link).then(response => response.json()),
    {
      onSuccess: branchCollection => {
        branchCollection._embedded.branches.forEach(branch => {
          // TODO does this make sense?
          // do we want every branch in the cache
          // it slows down the rendering of the branch chooser
          queryClient.setQueryData(branchQueryKey(repository, branch), branch);
        });
      }
    }
  );
};
