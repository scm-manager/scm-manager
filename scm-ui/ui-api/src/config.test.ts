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
