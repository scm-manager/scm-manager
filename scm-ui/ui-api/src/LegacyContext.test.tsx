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

import { LegacyContext, LegacyContextProvider, useLegacyContext } from "./LegacyContext";
import * as React from "react";
import { FC } from "react";
import { renderHook } from "@testing-library/react-hooks";
import { QueryClient, QueryClientProvider } from "react-query";

describe("LegacyContext tests", () => {
  const queryClient = new QueryClient();
  const createWrapper = (context?: LegacyContext): FC => {
    return ({ children }) => (
      <QueryClientProvider client={queryClient}>
        <LegacyContextProvider {...context}>{children}</LegacyContextProvider>
      </QueryClientProvider>
    );
  };

  it("should return provided context", () => {
    const { result } = renderHook(() => useLegacyContext(), {
      wrapper: createWrapper()
    });
    expect(result.current).toBeDefined();
  });

  it("should fail without providers", () => {
    const { result } = renderHook(() => useLegacyContext());
    expect(result.error).toBeDefined();
  });
});
