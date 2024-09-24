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

import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiClient } from "./apiclient";
import { useCallback } from "react";
import { HalRepresentation, Link } from "@scm-manager/ui-types";

type Result<C extends HalRepresentation> = {
  contentType: string;
  configuration: C;
};

type MutationVariables<C> = {
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
  } = useMutation<Response, Error, MutationVariables<Omit<C, keyof HalRepresentation>>>(
    (vars) => apiClient.put(vars.link, vars.configuration, vars.contentType),
    {
      onSuccess: async () => {
        await queryClient.invalidateQueries(queryKey);
      },
    }
  );

  const isReadOnly = !data?.configuration._links.update;

  const update = useCallback(
    (configuration: Omit<C, keyof HalRepresentation>) => {
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
