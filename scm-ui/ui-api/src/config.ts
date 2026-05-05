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
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "./apiclient";
import { requiredLink } from "./links";

export const useConfig = (): ApiResult<Config> => {
  const indexLink = useIndexLink("config");
  return useQuery<Config, Error>({
    queryKey: ["config"],
    queryFn: () => apiClient.get(indexLink!).then((response) => response.json()),
    enabled: !!indexLink,
  });
};

export const useUpdateConfig = () => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data, reset } = useMutation<unknown, Error, Config>({
    mutationFn: (config) => {
      const updateUrl = requiredLink(config, "update");
      return apiClient.put(updateUrl, config, "application/vnd.scmm-config+json;v=2");
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["config"] });
      await queryClient.invalidateQueries({ queryKey: ["index"] });
      await queryClient.invalidateQueries({ queryKey: ["pluginCenterAuth"] });
    },
  });
  return {
    update: (config: Config) => mutate(config),
    isPending,
    error,
    isUpdated: !!data,
    reset,
  };
};
