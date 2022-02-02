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
import { apiClient } from "./apiclient";
import { requiredLink } from "./links";
import { BadGatewayError } from "./errors";

type WaitForRestartOptions = {
  initialDelay?: number;
  timeout?: number;
};

const waitForRestartAfter = (
  promise: Promise<any>,
  { initialDelay = 1000, timeout = 500 }: WaitForRestartOptions = {}
): Promise<void> => {
  const endTime = Number(new Date()) + 4 * 60 * 1000;
  let started = false;

  const executor = <T = any>(data: T) => (resolve: (result: T) => void, reject: (error: Error) => void) => {
    // we need some initial delay
    if (!started) {
      started = true;
      setTimeout(executor(data), initialDelay, resolve, reject);
    } else {
      apiClient
        .get("")
        .then(() => resolve(data))
        .catch(() => {
          if (Number(new Date()) < endTime) {
            setTimeout(executor(data), timeout, resolve, reject);
          } else {
            reject(new Error("timeout reached"));
          }
        });
    }
  };

  return promise
    .catch(err => {
      if (err instanceof BadGatewayError) {
        // in some rare cases the reverse proxy stops forwarding traffic to scm before the response is returned
        // in such a case the reverse proxy returns 502 (bad gateway), so we treat 502 not as error
        return "ok";
      }
      throw err;
    })
    .then(data => new Promise<void>(executor(data)));
};

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

type RestartOptions = WaitForRestartOptions & {
  restart?: boolean;
};

type PluginActionOptions = {
  plugin: Plugin;
  restartOptions: RestartOptions;
};

export const useInstallPlugin = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, PluginActionOptions>(
    ({ plugin, restartOptions: { restart, ...waitForRestartOptions } }) => {
      const promise = apiClient.post(requiredLink(plugin, linkWithRestart("install", restart)));
      if (restart) {
        return waitForRestartAfter(promise, waitForRestartOptions);
      }
      return promise;
    },
    {
      onSuccess: () => queryClient.invalidateQueries("plugins")
    }
  );
  return {
    install: (plugin: Plugin, restartOptions: RestartOptions = {}) =>
      mutate({
        plugin,
        restartOptions
      }),
    isLoading,
    error,
    data,
    isInstalled: !!data
  };
};

export const useUninstallPlugin = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, PluginActionOptions>(
    ({ plugin, restartOptions: { restart, ...waitForRestartOptions } }) => {
      const promise = apiClient.post(requiredLink(plugin, linkWithRestart("uninstall", restart)));
      if (restart) {
        return waitForRestartAfter(promise, waitForRestartOptions);
      }
      return promise;
    },
    {
      onSuccess: () => queryClient.invalidateQueries("plugins")
    }
  );
  return {
    uninstall: (plugin: Plugin, restartOptions: RestartOptions = {}) =>
      mutate({
        plugin,
        restartOptions
      }),
    isLoading,
    error,
    isUninstalled: !!data
  };
};

type UpdatePluginsOptions = {
  plugins: Plugin | PluginCollection;
  restartOptions: RestartOptions;
};

export const useUpdatePlugins = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, UpdatePluginsOptions>(
    ({ plugins, restartOptions: { restart, ...waitForRestartOptions } }) => {
      const isCollection = isPluginCollection(plugins);
      const promise = apiClient.post(
        requiredLink(plugins, isCollection ? "update" : linkWithRestart("update", restart))
      );
      if (restart && !isCollection) {
        return waitForRestartAfter(promise, waitForRestartOptions);
      }
      return promise;
    },
    {
      onSuccess: () => queryClient.invalidateQueries("plugins")
    }
  );
  return {
    update: (plugin: Plugin | PluginCollection, restartOptions: RestartOptions = {}) =>
      mutate({
        plugins: plugin,
        restartOptions
      }),
    isLoading,
    error,
    isUpdated: !!data
  };
};

type ExecutePendingPlugins = {
  pending: PendingPlugins;
  restartOptions: WaitForRestartOptions;
};

export const useExecutePendingPlugins = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, ExecutePendingPlugins>(
    ({ pending, restartOptions }) =>
      waitForRestartAfter(apiClient.post(requiredLink(pending, "execute")), restartOptions),
    {
      onSuccess: () => queryClient.invalidateQueries("plugins")
    }
  );
  return {
    update: (pending: PendingPlugins, restartOptions: WaitForRestartOptions = {}) =>
      mutate({ pending, restartOptions }),
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
