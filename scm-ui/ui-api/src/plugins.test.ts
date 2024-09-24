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
import fetchMock from "fetch-mock-jest";
import { renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";
import {
  useAvailablePlugins,
  useInstalledPlugins,
  useInstallPlugin,
  usePendingPlugins,
  useUninstallPlugin,
  useUpdatePlugins,
} from "./plugins";
import { act } from "react-test-renderer";

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
    type: "SCM",
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
    type: "SCM",
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
    type: "SCM",
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
    uninstallPlugins: Plugin[] = []
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
      const { result, waitFor } = renderHook(() => useAvailablePlugins(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(availablePlugins);
    });
  });

  describe("useInstalledPlugins tests", () => {
    it("should return installedPlugins", async () => {
      const installedPlugins = createPluginCollection([installedPlugin, installedCorePlugin]);
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "installedPlugins", "/installedPlugins");
      fetchMock.get("/api/v2/installedPlugins", installedPlugins);
      const { result, waitFor } = renderHook(() => useInstalledPlugins(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(installedPlugins);
    });
  });

  describe("usePendingPlugins tests", () => {
    it("should return pendingPlugins", async () => {
      const pendingPlugins = createPendingPlugins([availablePlugin]);
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "pendingPlugins", "/pendingPlugins");
      fetchMock.get("/api/v2/pendingPlugins", pendingPlugins);
      const { result, waitFor } = renderHook(() => usePendingPlugins(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(pendingPlugins);
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
      const { result, waitFor, waitForNextUpdate } = renderHook(() => useInstallPlugin(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await act(() => {
        const { install } = result.current;
        install(availablePlugin, { restart: true, initialDelay: 5, timeout: 5 });
        return waitForNextUpdate();
      });
      await waitFor(() => result.current.isInstalled);
      expect(queryClient.getQueryState(["plugins", "available"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "installed"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "pending"])!.isInvalidated).toBe(true);
    });

    it("should invalidate query keys", async () => {
      const queryClient = createInfiniteCachingClient();
      queryClient.setQueryData(["plugins", "available"], createPluginCollection([availablePlugin]));
      queryClient.setQueryData(["plugins", "installed"], createPluginCollection([]));
      queryClient.setQueryData(["plugins", "pending"], createPendingPlugins());
      fetchMock.post("/api/v2/plugins/available/heart-of-gold-plugin/install", installedPlugin);
      const { result, waitForNextUpdate } = renderHook(() => useInstallPlugin(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await act(() => {
        const { install } = result.current;
        install(availablePlugin);
        return waitForNextUpdate();
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
      const { result, waitForNextUpdate, waitFor } = renderHook(() => useUninstallPlugin(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await act(() => {
        const { uninstall } = result.current;
        uninstall(installedPlugin, { restart: true, initialDelay: 5, timeout: 5 });
        return waitForNextUpdate();
      });
      await waitFor(() => result.current.isUninstalled);
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
      const { result, waitForNextUpdate } = renderHook(() => useUninstallPlugin(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await act(() => {
        const { uninstall } = result.current;
        uninstall(installedPlugin);
        return waitForNextUpdate();
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
      const { result, waitForNextUpdate, waitFor } = renderHook(() => useUpdatePlugins(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await act(() => {
        const { update } = result.current;
        update(installedPlugin, { restart: true, timeout: 5, initialDelay: 5 });
        return waitForNextUpdate();
      });
      await waitFor(() => result.current.isUpdated);
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
      const { result, waitForNextUpdate } = renderHook(() => useUpdatePlugins(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await act(() => {
        const { update } = result.current;
        update(createPluginCollection([installedPlugin, installedCorePlugin]));
        return waitForNextUpdate();
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
      const { result, waitForNextUpdate } = renderHook(() => useUpdatePlugins(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await act(() => {
        const { update } = result.current;
        update(createPluginCollection([installedPlugin, installedCorePlugin]), { restart: true });
        return waitForNextUpdate();
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
      const { result, waitForNextUpdate } = renderHook(() => useUpdatePlugins(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await act(() => {
        const { update } = result.current;
        update(installedPlugin);
        return waitForNextUpdate();
      });
      expect(queryClient.getQueryState(["plugins", "available"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "installed"])!.isInvalidated).toBe(true);
      expect(queryClient.getQueryState(["plugins", "pending"])!.isInvalidated).toBe(true);
    });
  });
});
