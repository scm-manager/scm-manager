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

import { Config } from "@scm-manager/ui-types";
import fetchMock from "fetch-mock-jest";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { setIndexLink } from "./tests/indexLinks";
import { renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";
import { useConfig, useUpdateConfig } from "./config";
import { act } from "react-test-renderer";

describe("Test config hooks", () => {
  const config: Config = {
    anonymousAccessEnabled: false,
    anonymousMode: "OFF",
    baseUrl: "",
    dateFormat: "",
    disableGroupingGrid: false,
    enableProxy: false,
    enabledUserConverter: false,
    enabledXsrfProtection: false,
    enabledApiKeys: false,
    enabledFileSearch: true,
    forceBaseUrl: false,
    loginAttemptLimit: 0,
    loginAttemptLimitTimeout: 0,
    loginInfoUrl: "",
    mailDomainName: "",
    namespaceStrategy: "",
    emergencyContacts: [],
    pluginUrl: "",
    pluginAuthUrl: "",
    proxyExcludes: [],
    proxyPassword: null,
    proxyPort: 0,
    proxyServer: "",
    proxyUser: null,
    realmDescription: "",
    alertsUrl: "",
    releaseFeedUrl: "",
    skipFailedAuthenticators: false,
    _links: {
      update: {
        href: "/config",
      },
    },
  };

  afterEach(() => {
    fetchMock.reset();
  });

  describe("useConfig tests", () => {
    it("should return config", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "config", "/config");
      fetchMock.get("/api/v2/config", config);
      const { result, waitFor } = renderHook(() => useConfig(), {
        wrapper: createWrapper(undefined, queryClient),
      });
      await waitFor(() => !!result.current.data);
      expect(result.current.data).toEqual(config);
    });
  });

  describe("useUpdateConfig tests", () => {
    it("should update config", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "config", "/config");

      const newConfig = {
        ...config,
        baseUrl: "/hog",
      };

      fetchMock.putOnce("/api/v2/config", {
        status: 200,
      });

      const { result, waitForNextUpdate } = renderHook(() => useUpdateConfig(), {
        wrapper: createWrapper(undefined, queryClient),
      });

      await act(() => {
        const { update } = result.current;
        update(newConfig);
        return waitForNextUpdate();
      });

      expect(result.current.error).toBeFalsy();
      expect(result.current.isUpdated).toBe(true);
      expect(result.current.isLoading).toBe(false);
      expect(queryClient.getQueryData(["config"])).toBeUndefined();
    });
  });
});
