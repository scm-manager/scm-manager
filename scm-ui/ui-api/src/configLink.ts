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

import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiClient } from "./apiclient";
import { useCallback } from "react";
import { HalRepresentation, Link } from "@scm-manager/ui-types";

type Result<C extends HalRepresentation> = {
  contentType: string;
  configuration: C;
};

type MutationVariables<C extends HalRepresentation> = {
  configuration: C;
  contentType: string;
  link: string;
};

export const useConfigLink = <C extends HalRepresentation>(link: string) => {
  const queryClient = useQueryClient();
  const queryKey = ["configLink", link];
  const { isLoading, error, data } = useQuery<Result<C>, Error>(queryKey, () =>
    apiClient.get(link).then((response) => {
      const contentType = response.headers.get("Content-Type") || "application/json";
      return response.json().then((configuration: C) => ({ configuration, contentType }));
    })
  );

  const {
    isLoading: isUpdating,
    error: mutationError,
    mutateAsync,
    data: updateResponse,
  } = useMutation<Response, Error, MutationVariables<C>>(
    (vars: MutationVariables<C>) => apiClient.put(vars.link, vars.configuration, vars.contentType),
    {
      onSuccess: async () => {
        await queryClient.invalidateQueries(queryKey);
      },
    }
  );

  const isReadOnly = !data?.configuration._links.update;

  const update = useCallback(
    (configuration: C) => {
      if (data && !isReadOnly) {
        return mutateAsync({
          configuration,
          contentType: data.contentType,
          link: (data.configuration._links.update as Link).href,
        });
      }
    },
    // eslint means we should add C to the dependency array, but C is only a type
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [mutateAsync, data, isReadOnly]
  );

  return {
    isLoading,
    isUpdating,
    isReadOnly,
    error: error || mutationError,
    initialConfiguration: data?.configuration,
    update,
    isUpdated: !!updateResponse,
  };
};
