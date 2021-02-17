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

import { ApiResult, useIndexLink, useRequiredIndexLink } from "./base";
import { isPluginCollection, PendingPlugins, Plugin, PluginCollection } from "@scm-manager/ui-types";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiClient } from "@scm-manager/ui-components";
import { requiredLink } from "./links";

export type UseAvailablePluginsOptions = {
  enabled?: boolean;
};

export const useAvailablePlugins = ({ enabled }: UseAvailablePluginsOptions = {}): ApiResult<PluginCollection> => {
  const indexLink = useRequiredIndexLink("availablePlugins");
  return useQuery<PluginCollection, Error>(
    ["plugins", "available"],
    () => apiClient.get(indexLink).then(response => response.json()),
    {
      enabled,
      retry: 3
    }
  );
};

export type UseInstalledPluginsOptions = {
  enabled?: boolean;
};

export const useInstalledPlugins = ({ enabled }: UseInstalledPluginsOptions = {}): ApiResult<PluginCollection> => {
  const indexLink = useRequiredIndexLink("installedPlugins");
  return useQuery<PluginCollection, Error>(
    ["plugins", "installed"],
    () => apiClient.get(indexLink).then(response => response.json()),
    {
      enabled,
      retry: 3
    }
  );
};

export const usePendingPlugins = (): ApiResult<PendingPlugins> => {
  const indexLink = useIndexLink("pendingPlugins");
  return useQuery<PendingPlugins, Error>(
    ["plugins", "pending"],
    () => apiClient.get(indexLink!).then(response => response.json()),
    {
      enabled: !!indexLink,
      retry: 3
    }
  );
};

const linkWithRestart = (link: string, restart?: boolean) => {
  if (restart) {
    return link + "WithRestart";
  }
  return link;
};

type PluginActionOptions = {
  plugin: Plugin;
  restart?: boolean;
};

export const useInstallPlugin = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, PluginActionOptions>(
    ({ plugin, restart }) => apiClient.post(requiredLink(plugin, linkWithRestart("install", restart))),
    {
      onSuccess: () => queryClient.invalidateQueries("plugins")
    }
  );
  return {
    install: (plugin: Plugin, restart?: boolean) => mutate({ plugin, restart }),
    isLoading,
    error,
    isInstalled: !!data
  };
};

export const useUninstallPlugin = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, PluginActionOptions>(
    ({ plugin, restart }) => apiClient.post(requiredLink(plugin, linkWithRestart("uninstall", restart))),
    {
      onSuccess: () => queryClient.invalidateQueries("plugins")
    }
  );
  return {
    uninstall: (plugin: Plugin, restart?: boolean) => mutate({ plugin, restart }),
    isLoading,
    error,
    isUninstalled: !!data
  };
};

type UpdatePluginsOptions = {
  plugins: Plugin | PluginCollection;
  restart?: boolean;
};

export const useUpdatePlugins = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, UpdatePluginsOptions>(
    ({ plugins, restart }) =>
      apiClient.post(
        requiredLink(plugins, isPluginCollection(plugins) ? "update" : linkWithRestart("update", restart))
      ),
    {
      onSuccess: () => queryClient.invalidateQueries("plugins")
    }
  );
  return {
    update: (plugin: Plugin | PluginCollection, restart?: boolean) => mutate({ plugins: plugin, restart }),
    isLoading,
    error,
    isUpdated: !!data
  };
};

export const useExecutePendingPlugins = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, PendingPlugins>(
    pending => apiClient.post(requiredLink(pending, "execute")),
    {
      onSuccess: () => queryClient.invalidateQueries("plugins")
    }
  );
  return {
    update: (pending: PendingPlugins) => mutate(pending),
    isLoading,
    error,
    isExecuted: !!data
  };
};

export const useCancelPendingPlugins = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, PendingPlugins>(
    pending => apiClient.post(requiredLink(pending, "cancel")),
    {
      onSuccess: () => queryClient.invalidateQueries("plugins")
    }
  );
  return {
    update: (pending: PendingPlugins) => mutate(pending),
    isLoading,
    error,
    isCancelled: !!data
  };
};
