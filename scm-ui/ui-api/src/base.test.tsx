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

import fetchMock from "fetch-mock";
import { useIndex, useIndexJsonResource, useIndexLink, useIndexLinks, useRequiredIndexLink, useVersion } from "./base";
import { render, renderHook, waitFor, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import { Link } from "@scm-manager/ui-types";
import createWrapper from "./tests/createWrapper";
import { QueryClient } from "@tanstack/react-query";
import React, { FC } from "react";

describe("Test base api hooks", () => {
  describe("useIndex tests", () => {
    fetchMock.get("/api/v2/", {
      version: "x.y.z",
      _links: {},
    });

    it("should return index", async () => {
      const { result } = renderHook(() => useIndex(), { wrapper: createWrapper() });
      await waitFor(() => {
        return expect(result.current.version).toEqual("x.y.z");
      });
    });
  });

  describe("useIndexLink tests", () => {
    it("should return undefined for unknown link", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData(["index"], {
        version: "x.y.z",
        _links: {},
      });
      const { result } = renderHook(() => useIndexLink("spaceships"), {
        wrapper: createWrapper(queryClient),
      });
      expect(result.current).toBeUndefined();
    });

    it("should return undefined for link array", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData(["index"], {
        version: "x.y.z",
        _links: {
          spaceships: [
            {
              name: "heartOfGold",
              href: "/spaceships/heartOfGold",
            },
            {
              name: "razorCrest",
              href: "/spaceships/razorCrest",
            },
          ],
        },
      });
      const { result } = renderHook(() => useIndexLink("spaceships"), {
        wrapper: createWrapper(queryClient),
      });
      expect(result.current).toBeUndefined();
    });

    it("should return link", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData(["index"], {
        version: "x.y.z",
        _links: {
          spaceships: {
            href: "/api/spaceships",
          },
        },
      });
      const { result } = renderHook(() => useIndexLink("spaceships"), {
        wrapper: createWrapper(queryClient),
      });
      expect(result.current).toBe("/api/spaceships");
    });
  });

  describe("useIndexLinks tests", () => {
    it("should throw an error if index is not available", async () => {
      const queryClient = new QueryClient();

      const TestComponent: FC = () => {
        const link = useIndexLinks();

        if (!link || Object.keys(link).length === 0) {
          throw new Error("Index not available");
        }

        return <>{JSON.stringify(link)}</>;
      };

      const Wrapper = createWrapper(queryClient);

      render(
        <Wrapper>
          <TestComponent />
        </Wrapper>,
      );

      await waitFor(() => {
        expect(screen.getByRole("heading")).toHaveTextContent("ERROR");
      });
    });

    it("should return links", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData(["index"], {
        version: "x.y.z",
        _links: {
          spaceships: {
            href: "/api/spaceships",
          },
        },
      });
      const { result } = renderHook(() => useIndexLinks(), {
        wrapper: createWrapper(queryClient),
      });
      expect((result.current!.spaceships as Link).href).toBe("/api/spaceships");
    });
  });

  describe("useVersion tests", () => {
    it("should throw an error if version is not available", async () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData(["index"], {
        version: "",
      });

      const TestComponent: FC = () => {
        const link = useVersion();
        return <>{link}</>;
      };

      const Wrapper = createWrapper(queryClient);

      render(
        <Wrapper>
          <TestComponent />
        </Wrapper>,
      );

      await waitFor(() => {
        expect(screen.getByRole("heading")).toHaveTextContent("ERROR");
      });
    });

    it("should return version", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData(["index"], {
        version: "x.y.z",
      });
      const { result } = renderHook(() => useVersion(), {
        wrapper: createWrapper(queryClient),
      });
      expect(result.current).toBe("x.y.z");
    });
  });

  describe("useRequiredIndexLink tests", () => {
    it("should throw error for undefined link", async () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData(["index"], {
        version: "x.y.z",
        _links: {},
      });

      const TestComponent: FC = () => {
        const link = useRequiredIndexLink("spaceships");
        return <>{link}</>;
      };

      const Wrapper = createWrapper(queryClient);

      render(
        <Wrapper>
          <TestComponent />
        </Wrapper>,
      );

      await waitFor(() => {
        expect(screen.getByRole("heading")).toHaveTextContent("ERROR");
      });
    });
    it("should return link", () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData(["index"], {
        version: "x.y.z",
        _links: {
          spaceships: {
            href: "/api/spaceships",
          },
        },
      });
      const { result } = renderHook(() => useRequiredIndexLink("spaceships"), {
        wrapper: createWrapper(queryClient),
      });
      expect(result.current).toBe("/api/spaceships");
    });
  });

  describe("useIndexJsonResource tests", () => {
    it("should return json resource from link", async () => {
      const queryClient = new QueryClient();
      queryClient.setQueryData(["index"], {
        version: "x.y.z",
        _links: {
          spaceships: {
            href: "/spaceships",
          },
        },
      });

      const spaceship = {
        name: "heartOfGold",
      };

      fetchMock.get("/api/v2/spaceships", spaceship);

      const { result } = renderHook(() => useIndexJsonResource<typeof spaceship>("spaceships"), {
        wrapper: createWrapper(queryClient),
      });

      await waitFor(() => {
        expect(result.current.data!.name).toBe("heartOfGold");
      });
    });
  });

  it("should return nothing if link is not available", () => {
    const queryClient = new QueryClient();
    queryClient.setQueryData(["index"], {
      version: "x.y.z",
      _links: {},
    });

    const { result } = renderHook(() => useIndexJsonResource<Record<string, unknown>>("spaceships"), {
      wrapper: createWrapper(queryClient),
    });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.error).toBeFalsy();
    expect(result.current.data).toBeFalsy();
  });
});
