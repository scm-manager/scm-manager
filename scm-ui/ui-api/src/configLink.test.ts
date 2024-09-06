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

import fetchMock from "fetch-mock-jest";
import { HalRepresentation } from "@scm-manager/ui-types";
import { renderHook } from "@testing-library/react-hooks";
import createWrapper from "./tests/createWrapper";
import { useConfigLink } from "./configLink";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import { act } from "react-test-renderer";

describe("useConfigLink tests", () => {
  type MyConfig = HalRepresentation & {
    name: string;
  };

  const myReadOnlyConfig: MyConfig = {
    name: "Hansolo",
    _links: {},
  };

  const myConfig: MyConfig = {
    name: "Lea",
    _links: {
      update: {
        href: "/my/config-write",
      },
    },
  };

  afterEach(() => {
    fetchMock.reset();
  });

  const fetchConfiguration = async (config: MyConfig) => {
    fetchMock.getOnce("/api/v2/my/config", config);

    const queryClient = createInfiniteCachingClient();

    const { result, waitFor } = renderHook(() => useConfigLink<MyConfig>("/my/config"), {
      wrapper: createWrapper(undefined, queryClient),
    });
    await waitFor(() => {
      return !!result.current.initialConfiguration;
    });

    return result.current;
  };

  it("should return read only configuration without update link", async () => {
    const { isReadOnly } = await fetchConfiguration(myReadOnlyConfig);

    expect(isReadOnly).toBe(true);
  });

  it("should not be read only with update link", async () => {
    const { isReadOnly } = await fetchConfiguration(myConfig);

    expect(isReadOnly).toBe(false);
  });

  it("should call update url", async () => {
    const queryClient = createInfiniteCachingClient();

    fetchMock.get("/api/v2/my/config", myConfig);

    const { result, waitFor, waitForNextUpdate } = renderHook(() => useConfigLink<MyConfig>("/my/config"), {
      wrapper: createWrapper(undefined, queryClient),
    });

    await waitFor(() => {
      return !!result.current.initialConfiguration;
    });

    const { update } = result.current;

    const lukesConfig = {
      ...myConfig,
      name: "Luke",
    };

    fetchMock.putOnce(
      {
        url: "/api/v2/my/config-write",
        headers: {
          "Content-Type": "application/json",
        },
        body: lukesConfig,
      },
      {
        status: 204,
      }
    );

    await act(() => {
      update(lukesConfig);
      return waitForNextUpdate();
    });

    expect(result.current.isUpdated).toBe(true);
  });

  it("should capture content type update url", async () => {
    const queryClient = createInfiniteCachingClient();

    fetchMock.get("/api/v2/my/config", {
      headers: {
        "Content-Type": "application/myconfig+json",
      },
      body: myConfig,
    });

    const { result, waitFor, waitForNextUpdate } = renderHook(() => useConfigLink<MyConfig>("/my/config"), {
      wrapper: createWrapper(undefined, queryClient),
    });

    await waitFor(() => {
      return !!result.current.initialConfiguration;
    });

    const { update } = result.current;

    const lukesConfig = {
      ...myConfig,
      name: "Luke",
    };

    fetchMock.putOnce(
      {
        url: "/api/v2/my/config-write",
        headers: {
          "Content-Type": "application/myconfig+json",
        },
        body: lukesConfig,
      },
      {
        status: 204,
      }
    );

    await act(() => {
      update(lukesConfig);
      return waitForNextUpdate();
    });

    expect(result.current.isUpdated).toBe(true);
  });
});
