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

import { ApiResult, useIndexLink, useRequiredIndexLink } from "./base";
import type { PendingPlugins, Plugin, PluginCollection, HalRepresentation } from "@scm-manager/ui-types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "./apiclient";
import { requiredLink } from "./links";
import { BadGatewayError } from "./errors";

const isPluginCollection = (input: HalRepresentation): input is PluginCollection =>
  input._embedded ? "plugins" in input._embedded : false;

type WaitForRestartOptions = {
  initialDelay?: number;
  timeout?: number;
};

export const waitForRestartAfter = (
  promise: Promise<any>,
  { initialDelay = 1000, timeout = 500 }: WaitForRestartOptions = {},
): Promise<void> => {
  const endTime = Number(new Date()) + 4 * 60 * 1000;
  let started = false;

  const executor =
    <T = any>(data: T) =>
    (resolve: (result: T) => void, reject: (error: Error) => void) => {
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
    .catch((err) => {
      if (err instanceof BadGatewayError) {
        // in some rare cases the reverse proxy stops forwarding traffic to scm before the response is returned
        // in such a case the reverse proxy returns 502 (bad gateway), so we treat 502 not as error
        return "ok";
      }
      throw err;
    })
    .then((data) => new Promise<void>(executor(data)));
};

export type UseAvailablePluginsOptions = {
  enabled?: boolean;
};

export const useAvailablePlugins = ({ enabled }: UseAvailablePluginsOptions = {}): ApiResult<PluginCollection> => {
  const indexLink = useRequiredIndexLink("availablePlugins");
  return useQuery<PluginCollection, Error>({
    queryKey: ["plugins", "available"],
    queryFn: () => apiClient.get(indexLink).then((response) => response.json()),
    enabled,
    retry: 3,
  });
};

export type UseInstalledPluginsOptions = {
  enabled?: boolean;
};

export const useInstalledPlugins = ({ enabled }: UseInstalledPluginsOptions = {}): ApiResult<PluginCollection> => {
  const indexLink = useRequiredIndexLink("installedPlugins");
  return useQuery<PluginCollection, Error>({
    queryKey: ["plugins", "installed"],
    queryFn: () => apiClient.get(indexLink).then((response) => response.json()),
    enabled,
    retry: 3,
  });
};

export const usePendingPlugins = (): ApiResult<PendingPlugins> => {
  const indexLink = useIndexLink("pendingPlugins");
  return useQuery<PendingPlugins, Error>({
    queryKey: ["plugins", "pending"],
    queryFn: () => apiClient.get(indexLink!).then((response) => response.json()),
    enabled: !!indexLink,
    retry: 3,
  });
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
  const { mutate, isPending, error, data } = useMutation<unknown, Error, PluginActionOptions>({
    mutationFn: ({ plugin, restartOptions: { restart, ...waitForRestartOptions } }) => {
      const promise = apiClient.post(requiredLink(plugin, linkWithRestart("install", restart)));
      if (restart) {
        return waitForRestartAfter(promise, waitForRestartOptions);
      }
      return promise;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["plugins"] }),
  });
  return {
    install: (plugin: Plugin, restartOptions: RestartOptions = {}) =>
      mutate({
        plugin,
        restartOptions,
      }),
    isPending,
    error,
    data,
    isInstalled: !!data,
  };
};

export const useUninstallPlugin = () => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data } = useMutation<unknown, Error, PluginActionOptions>({
    mutationFn: ({ plugin, restartOptions: { restart, ...waitForRestartOptions } }) => {
      const promise = apiClient.post(requiredLink(plugin, linkWithRestart("uninstall", restart)));
      if (restart) {
        return waitForRestartAfter(promise, waitForRestartOptions);
      }
      return promise;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["plugins"] }),
  });
  return {
    uninstall: (plugin: Plugin, restartOptions: RestartOptions = {}) =>
      mutate({
        plugin,
        restartOptions,
      }),
    isPending,
    error,
    isUninstalled: !!data,
  };
};

type UpdatePluginsOptions = {
  plugins: Plugin | PluginCollection;
  restartOptions: RestartOptions;
};

export const useUpdatePlugins = () => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data } = useMutation<unknown, Error, UpdatePluginsOptions>({
    mutationFn: ({ plugins, restartOptions: { restart, ...waitForRestartOptions } }) => {
      const isCollection = isPluginCollection(plugins);
      const promise = apiClient.post(
        requiredLink(plugins, isCollection ? "update" : linkWithRestart("update", restart)),
      );
      if (restart && !isCollection) {
        return waitForRestartAfter(promise, waitForRestartOptions);
      }
      return promise;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["plugins"] }),
  });
  return {
    update: (plugin: Plugin | PluginCollection, restartOptions: RestartOptions = {}) =>
      mutate({
        plugins: plugin,
        restartOptions,
      }),
    isPending,
    error,
    isUpdated: !!data,
  };
};

type ExecutePendingPlugins = {
  pending: PendingPlugins;
  restartOptions: WaitForRestartOptions;
};

export const useExecutePendingPlugins = () => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data } = useMutation<unknown, Error, ExecutePendingPlugins>({
    mutationFn: ({ pending, restartOptions }) =>
      waitForRestartAfter(apiClient.post(requiredLink(pending, "execute")), restartOptions),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["plugins"] }),
  });
  return {
    update: (pending: PendingPlugins, restartOptions: WaitForRestartOptions = {}) =>
      mutate({ pending, restartOptions }),
    isPending,
    error,
    isExecuted: !!data,
  };
};

export const useCancelPendingPlugins = () => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data } = useMutation<unknown, Error, PendingPlugins>({
    mutationFn: (pending) => apiClient.post(requiredLink(pending, "cancel")),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["plugins"] }),
  });
  return {
    update: (pending: PendingPlugins) => mutate(pending),
    isPending,
    error,
    isCancelled: !!data,
  };
};
