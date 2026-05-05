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

import { PendingPlugins, Plugin, PluginCollection } from "@scm-manager/ui-types";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { setIndexLink } from "./tests/indexLinks";
import fetchMock from "fetch-mock";
import createWrapper from "./tests/createWrapper";
import {
  useAvailablePlugins,
  useInstalledPlugins,
  useInstallPlugin,
  usePendingPlugins,
  useUninstallPlugin,
  useUpdatePlugins,
} from "./plugins";
import { renderHook, act, waitFor } from "@testing-library/react";

describe("Test plugin hooks", () => {
  const availablePlugin: Plugin = {
    author: "Douglas Adams",
    category: "all",
    displayName: "Heart of Gold",
    version: "x.y.z",
    name: "heart-of-gold-plugin",
    pending: false,
    dependencies: [],
    optionalDependencies: [],
    _links: {
      install: { href: "/plugins/available/heart-of-gold-plugin/install" },
      installWithRestart: {
        href: "/plugins/available/heart-of-gold-plugin/install?restart=true",
      },
    },
  };

  const installedPlugin: Plugin = {
    author: "Douglas Adams",
    category: "all",
    displayName: "Heart of Gold",
    version: "x.y.z",
    name: "heart-of-gold-plugin",
    pending: false,
    markedForUninstall: false,
    dependencies: [],
    optionalDependencies: [],
    _links: {
      self: {
        href: "/plugins/installed/heart-of-gold-plugin",
      },
      update: {
        href: "/plugins/available/heart-of-gold-plugin/install",
      },
      updateWithRestart: {
        href: "/plugins/available/heart-of-gold-plugin/install?restart=true",
      },
      uninstall: {
        href: "/plugins/installed/heart-of-gold-plugin/uninstall",
      },
      uninstallWithRestart: {
        href: "/plugins/installed/heart-of-gold-plugin/uninstall?restart=true",
      },
    },
  };

  const installedCorePlugin: Plugin = {
    author: "Douglas Adams",
    category: "all",
    displayName: "Heart of Gold",
    version: "x.y.z",
    name: "heart-of-gold-core-plugin",
    pending: false,
    markedForUninstall: false,
    dependencies: [],
    optionalDependencies: [],
    _links: {
      self: {
        href: "/plugins/installed/heart-of-gold-core-plugin",
      },
    },
  };

  const createPluginCollection = (plugins: Plugin[]): PluginCollection => ({
    _links: {
      update: {
        href: "/plugins/update",
      },
    },
    _embedded: {
      plugins,
    },
    pluginCenterStatus: "OK",
  });

  const createPendingPlugins = (
    newPlugins: Plugin[] = [],
    updatePlugins: Plugin[] = [],
    uninstallPlugins: Plugin[] = [],
  ): PendingPlugins => ({
    _links: {},
    _embedded: {
      new: newPlugins,
      update: updatePlugins,
      uninstall: uninstallPlugins,
    },
  });

  afterEach(() => fetchMock.reset());

  describe("useAvailablePlugins tests", () => {
    it("should return availablePlugins", async () => {
      const availablePlugins = createPluginCollection([availablePlugin]);
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "availablePlugins", "/availablePlugins");
      fetchMock.get("/api/v2/availablePlugins", availablePlugins);
      const { result } = renderHook(() => useAvailablePlugins(), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(availablePlugins));
    });
  });

  describe("useInstalledPlugins tests", () => {
    it("should return installedPlugins", async () => {
      const installedPlugins = createPluginCollection([installedPlugin, installedCorePlugin]);
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "installedPlugins", "/installedPlugins");
      fetchMock.get("/api/v2/installedPlugins", installedPlugins);
      const { result } = renderHook(() => useInstalledPlugins(), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(installedPlugins));
    });
  });

  describe("usePendingPlugins tests", () => {
    it("should return pendingPlugins", async () => {
      const pendingPlugins = createPendingPlugins([availablePlugin]);
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "pendingPlugins", "/pendingPlugins");
      fetchMock.get("/api/v2/pendingPlugins", pendingPlugins);
      const { result } = renderHook(() => usePendingPlugins(), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(pendingPlugins));
    });
  });

  describe("useInstallPlugin tests", () => {
    it("should use restart parameter", async () => {
      const queryClient = createInfiniteCachingClient();
      queryClient.setQueryData(["plugins", "available"], createPluginCollection([availablePlugin]));
      queryClient.setQueryData(["plugins", "installed"], createPluginCollection([]));
      queryClient.setQueryData(["plugins", "pending"], createPendingPlugins());
      fetchMock.post("/api/v2/plugins/available/heart-of-gold-plugin/install?restart=true", installedPlugin);
      fetchMock.get("/api/v2/", "Restarted");
      const { result } = renderHook(() => useInstallPlugin(), {
        wrapper: createWrapper(queryClient),
      });
      act(() => {
        const { install } = result.current;
        install(availablePlugin, { restart: true, initialDelay: 5, timeout: 5 });
      });
      await waitFor(() => {
        expect(queryClient.getQueryState(["plugins", "available"])!.isInvalidated).toBe(true);
        expect(queryClient.getQueryState(["plugins", "installed"])!.isInvalidated).toBe(true);
        expect(queryClient.getQueryState(["plugins", "pending"])!.isInvalidated).toBe(true);
      });
    });

    it("should invalidate query keys", async () => {
      const queryClient = createInfiniteCachingClient();
      queryClient.setQueryData(["plugins", "available"], createPluginCollection([availablePlugin]));
      queryClient.setQueryData(["plugins", "installed"], createPluginCollection([]));
      queryClient.setQueryData(["plugins", "pending"], createPendingPlugins());
      fetchMock.post("/api/v2/plugins/available/heart-of-gold-plugin/install", installedPlugin);
      const { result } = renderHook(() => useInstallPlugin(), {
        wrapper: createWrapper(queryClient),
      });
      await act(async () => {
        await result.current.install(availablePlugin);
      });
      expect(queryClient.getQueryState(["plugins", "available"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "installed"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "pending"])!.isInvalidated).toBe(true);
    });
  });

  describe("useUninstallPlugin tests", () => {
    it("should use restart parameter", async () => {
      const queryClient = createInfiniteCachingClient();
      queryClient.setQueryData(["plugins", "available"], createPluginCollection([]));
      queryClient.setQueryData(["plugins", "installed"], createPluginCollection([installedPlugin]));
      queryClient.setQueryData(["plugins", "pending"], createPendingPlugins());
      fetchMock.post("/api/v2/plugins/installed/heart-of-gold-plugin/uninstall?restart=true", availablePlugin);
      fetchMock.get("/api/v2/", "Restarted");
      const { result } = renderHook(() => useUninstallPlugin(), {
        wrapper: createWrapper(queryClient),
      });
      await act(async () => {
        await result.current.uninstall(installedPlugin, { restart: true, initialDelay: 5, timeout: 5 });
      });
      await waitFor(() => {
        expect(result.current.isUninstalled).toBe(true);
      });
      expect(queryClient.getQueryState(["plugins", "available"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "installed"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "pending"])!.isInvalidated).toBe(true);
    });

    it("should invalidate query keys", async () => {
      const queryClient = createInfiniteCachingClient();
      queryClient.setQueryData(["plugins", "available"], createPluginCollection([]));
      queryClient.setQueryData(["plugins", "installed"], createPluginCollection([installedPlugin]));
      queryClient.setQueryData(["plugins", "pending"], createPendingPlugins());
      fetchMock.post("/api/v2/plugins/installed/heart-of-gold-plugin/uninstall", availablePlugin);
      const { result } = renderHook(() => useUninstallPlugin(), {
        wrapper: createWrapper(queryClient),
      });
      await act(async () => {
        await result.current.uninstall(installedPlugin);
      });
      expect(queryClient.getQueryState(["plugins", "available"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "installed"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "pending"])!.isInvalidated).toBe(true);
    });
  });

  describe("useUpdatePlugins tests", () => {
    it("should use restart parameter", async () => {
      const queryClient = createInfiniteCachingClient();
      queryClient.setQueryData(["plugins", "available"], createPluginCollection([]));
      queryClient.setQueryData(["plugins", "installed"], createPluginCollection([installedPlugin]));
      queryClient.setQueryData(["plugins", "pending"], createPendingPlugins());
      fetchMock.post("/api/v2/plugins/available/heart-of-gold-plugin/install?restart=true", installedPlugin);
      fetchMock.get("/api/v2/", "Restarted");
      const { result } = renderHook(() => useUpdatePlugins(), {
        wrapper: createWrapper(queryClient),
      });
      await act(async () => {
        await result.current.update(installedPlugin, { restart: true, timeout: 5, initialDelay: 5 });
      });
      await waitFor(() => {
        expect(result.current.isUpdated).toBe(true);
      });
      expect(queryClient.getQueryState(["plugins", "available"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "installed"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "pending"])!.isInvalidated).toBe(true);
    });
    it("should update collection", async () => {
      const queryClient = createInfiniteCachingClient();
      queryClient.setQueryData(["plugins", "available"], createPluginCollection([]));
      queryClient.setQueryData(["plugins", "installed"], createPluginCollection([installedPlugin]));
      queryClient.setQueryData(["plugins", "pending"], createPendingPlugins());
      fetchMock.post("/api/v2/plugins/update", installedPlugin);
      const { result } = renderHook(() => useUpdatePlugins(), {
        wrapper: createWrapper(queryClient),
      });
      await act(async () => {
        await result.current.update(createPluginCollection([installedPlugin, installedCorePlugin]));
      });
      expect(queryClient.getQueryState(["plugins", "available"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "installed"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "pending"])!.isInvalidated).toBe(true);
    });
    it("should ignore restart parameter collection", async () => {
      const queryClient = createInfiniteCachingClient();
      queryClient.setQueryData(["plugins", "available"], createPluginCollection([]));
      queryClient.setQueryData(["plugins", "installed"], createPluginCollection([installedPlugin]));
      queryClient.setQueryData(["plugins", "pending"], createPendingPlugins());
      fetchMock.post("/api/v2/plugins/update", installedPlugin);
      const { result } = renderHook(() => useUpdatePlugins(), {
        wrapper: createWrapper(queryClient),
      });
      await act(async () => {
        await result.current.update(createPluginCollection([installedPlugin, installedCorePlugin]), { restart: true });
      });
      expect(queryClient.getQueryState(["plugins", "available"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "installed"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "pending"])!.isInvalidated).toBe(true);
    });
    it("should invalidate query keys", async () => {
      const queryClient = createInfiniteCachingClient();
      queryClient.setQueryData(["plugins", "available"], createPluginCollection([]));
      queryClient.setQueryData(["plugins", "installed"], createPluginCollection([installedPlugin]));
      queryClient.setQueryData(["plugins", "pending"], createPendingPlugins());
      fetchMock.post("/api/v2/plugins/available/heart-of-gold-plugin/install", installedPlugin);
      const { result } = renderHook(() => useUpdatePlugins(), {
        wrapper: createWrapper(queryClient),
      });
      await act(async () => {
        await result.current.update(installedPlugin);
      });
      expect(queryClient.getQueryState(["plugins", "available"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "installed"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "pending"])!.isInvalidated).toBe(true);
    });
  });
});
