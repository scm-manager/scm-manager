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
import fetchMock from "fetch-mock";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { setIndexLink } from "./tests/indexLinks";
import { renderHook, act, waitFor } from "@testing-library/react";
import createWrapper from "./tests/createWrapper";
import { useConfig, useUpdateConfig } from "./config";

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
    proxyExcludes: [],
    proxyPassword: null,
    proxyPort: 0,
    proxyServer: "",
    proxyUser: null,
    realmDescription: "",
    alertsUrl: "",
    releaseFeedUrl: "",
    skipFailedAuthenticators: false,
    jwtExpirationInH: 1,
    enabledJwtEndless: false,
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
      const { result } = renderHook(() => useConfig(), {
        wrapper: createWrapper(queryClient),
      });
      await waitFor(() => expect(result.current.data).toEqual(config));
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

      const { result } = renderHook(() => useUpdateConfig(), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        await result.current.update(newConfig);
      });

      expect(result.current.error).toBeFalsy();

      await waitFor(() => {
        expect(result.current.isUpdated).toBe(true);
        expect(result.current.isPending).toBe(false);
      });

      expect(queryClient.getQueryData(["config"])).toBeUndefined();
    });
  });
});
