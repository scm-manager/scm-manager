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

import { ApiResult, useIndexLink } from "./base";
import { Config } from "@scm-manager/ui-types";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiClient } from "./apiclient";
import { requiredLink } from "./links";

export const useConfig = (): ApiResult<Config> => {
  const indexLink = useIndexLink("config");
  return useQuery<Config, Error>("config", () => apiClient.get(indexLink!).then(response => response.json()), {
    enabled: !!indexLink
  });
};

export const useUpdateConfig = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data, reset } = useMutation<unknown, Error, Config>(
    config => {
      const updateUrl = requiredLink(config, "update");
      return apiClient.put(updateUrl, config, "application/vnd.scmm-config+json;v=2");
    },
    {
      onSuccess: async () => {
        await queryClient.invalidateQueries("config");
        await queryClient.invalidateQueries("index");
        await queryClient.invalidateQueries("pluginCenterAuth");
      }
    }
  );
  return {
    update: (config: Config) => mutate(config),
    isLoading,
    error,
    isUpdated: !!data,
    reset
  };
};
