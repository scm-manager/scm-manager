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

import { LegacyContext, useLegacyContext } from "./LegacyContext";
import * as React from "react";
import { FC } from "react";
import { renderHook } from "@testing-library/react-hooks";
import ApiProvider from "./ApiProvider";
import { useQueryClient } from "react-query";

describe("ApiProvider tests", () => {
  const createWrapper = (context: LegacyContext): FC => {
    return ({ children }) => <ApiProvider {...context}>{children}</ApiProvider>;
  };

  it("should register QueryClient", () => {
    const { result } = renderHook(() => useQueryClient(), {
      wrapper: createWrapper({ initialize: () => null })
    });
    expect(result.current).toBeDefined();
  });

  it("should pass legacy context QueryClient", () => {
    let msg: string;
    const onIndexFetched = () => {
      msg = "hello";
    };

    const { result } = renderHook(() => useLegacyContext(), {
      wrapper: createWrapper({ onIndexFetched, initialize: () => null })
    });

    if (result.current?.onIndexFetched) {
      result.current.onIndexFetched({ version: "a.b.c", _links: {}, instanceId: "123" });
    }

    expect(msg!).toEqual("hello");
  });
});
