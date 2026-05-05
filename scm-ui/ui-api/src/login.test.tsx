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

import "@testing-library/jest-dom";
import fetchMock from "fetch-mock";
import { renderHook, act, waitFor, render, screen } from "@testing-library/react";
import { Me } from "@scm-manager/ui-types";
import createWrapper from "./tests/createWrapper";
import { useLogin, useLogout, useMe, useRequiredMe, useSubject } from "./login";
import { setEmptyIndex, setIndexLink } from "./tests/indexLinks";
import createInfiniteCachingClient from "./tests/createInfiniteCachingClient";
import React, { FC } from "react";

describe("Test login hooks", () => {
  const tricia: Me = {
    name: "tricia",
    displayName: "Tricia",
    groups: [],
    _links: {},
  };

  describe("useMe tests", () => {
    fetchMock.get("/api/v2/me", {
      name: "tricia",
      displayName: "Tricia",
      groups: [],
      _links: {},
    });

    it("should return me", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "me", "/me");

      const { result } = renderHook(() => useMe(), { wrapper: createWrapper(queryClient) });
      await waitFor(() => {
        return expect(result.current?.data?.name).toEqual("tricia");
      });
    });

    it("should return nothing without me link", () => {
      const queryClient = createInfiniteCachingClient();
      setEmptyIndex(queryClient);

      const { result } = renderHook(() => useMe(), { wrapper: createWrapper(queryClient) });

      expect(result.current.isLoading).toBe(false);
      expect(result.current?.data).toBeFalsy();
      expect(result.current?.error).toBeFalsy();
    });
  });

  describe("useRequiredMe tests", () => {
    it("should return me", async () => {
      const queryClient = createInfiniteCachingClient();
      queryClient.setQueryData(["me"], tricia);
      setIndexLink(queryClient, "me", "/me");
      const { result } = renderHook(() => useRequiredMe(), { wrapper: createWrapper(queryClient) });
      await waitFor(() => {
        return expect(result.current?.name).toBe("tricia");
      });
    });

    it("should throw an error if me is not available", async () => {
      const queryClient = createInfiniteCachingClient();
      setEmptyIndex(queryClient);

      const TestComponent: FC = () => {
        const link = useRequiredMe();
        return <>{link.name}</>;
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
  });

  describe("useSubject tests", () => {
    it("should return authenticated subject", () => {
      const queryClient = createInfiniteCachingClient();
      setEmptyIndex(queryClient);
      queryClient.setQueryData(["me"], tricia);
      const { result } = renderHook(() => useSubject(), { wrapper: createWrapper(queryClient) });

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.isAnonymous).toBe(false);
      expect(result.current.me).toEqual(tricia);
    });

    it("should return anonymous subject", () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "login", "/login");
      queryClient.setQueryData(["me"], {
        name: "_anonymous",
        displayName: "Anonymous",
        groups: [],
        _links: {},
      });
      const { result } = renderHook(() => useSubject(), { wrapper: createWrapper(queryClient) });

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.isAnonymous).toBe(true);
    });

    it("should return unauthenticated subject", () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "login", "/login");
      const { result } = renderHook(() => useSubject(), { wrapper: createWrapper(queryClient) });

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.isAnonymous).toBe(false);
    });
  });

  describe("useLogin tests", () => {
    it("should login", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "login", "/login");

      fetchMock.post("/api/v2/login", {
        body: {
          cookie: true,
          grant_type: "password",
          username: "tricia",
          password: "hitchhikersSecret!",
        },
      });

      // required because we invalidate the whole cache and react-query refetches the index
      fetchMock.get("/api/v2/", {
        version: "x.y.z",
        _links: {
          login: {
            href: "/second/login",
          },
        },
      });

      const { result } = renderHook(() => useLogin("login", () => {}), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        // @ts-ignore
        await result.current.login("tricia", "hitchhikersSecret!");
      });

      expect(result.current.error).toBeFalsy();
    });

    it("should not return login, if authenticated", () => {
      const queryClient = createInfiniteCachingClient();
      setEmptyIndex(queryClient);
      queryClient.setQueryData(["me"], tricia);

      const { result } = renderHook(() => useLogin("", () => {}), {
        wrapper: createWrapper(queryClient),
      });

      expect(result.current.login).toBeUndefined();
    });
  });

  describe("useLogout tests", () => {
    it("should call logout", async () => {
      const queryClient = createInfiniteCachingClient();
      setIndexLink(queryClient, "logout", "/logout");

      fetchMock.deleteOnce("/api/v2/logout", {});

      const { result } = renderHook(() => useLogout("logout"), {
        wrapper: createWrapper(queryClient),
      });

      await act(async () => {
        // @ts-ignore
        await result.current.logout();
      });

      expect(result.current.error).toBeFalsy();
    });

    it("should not return logout without link", () => {
      const queryClient = createInfiniteCachingClient();
      setEmptyIndex(queryClient);

      const { result } = renderHook(() => useLogout(""), {
        wrapper: createWrapper(queryClient),
      });

      const { logout } = result.current;
      expect(logout).toBeUndefined();
    });
  });
});
