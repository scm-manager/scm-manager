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
