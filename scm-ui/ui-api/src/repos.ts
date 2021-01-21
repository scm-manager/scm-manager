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

import { Me, Namespace, NamespaceCollection, RepositoryCollection, Link } from "@scm-manager/ui-types";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiClient } from "@scm-manager/ui-components";
import { ApiResult, useIndexJsonResource, useIndexLink, useRequiredIndexLink } from "./base";

export const useNamespaces = () => {
  return useIndexJsonResource<NamespaceCollection>("namespaces");
};

export const useRepositories = (namespace?: Namespace, page?: number | string, enabled?: boolean): ApiResult<RepositoryCollection> => {
  const indexLink = useRequiredIndexLink("repositories");
  const namespaceLink = (namespace?._links.repositories as Link)?.href;

  const link = namespaceLink || indexLink;
  return useQuery<RepositoryCollection, Error>(["repositories", namespace?.namespace, page], () =>
    apiClient.get(link).then(response => response.json()), {
      enabled: enabled || enabled === undefined
    }
  );
};
