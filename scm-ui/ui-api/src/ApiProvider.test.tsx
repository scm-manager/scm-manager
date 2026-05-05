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

import * as React from "react";
import { FC, ReactNode } from "react";
import { renderHook } from "@testing-library/react";
import ApiProvider from "./ApiProvider";
import { useQueryClient } from "@tanstack/react-query";

describe("ApiProvider tests", () => {
  const createWrapper = (): FC<{ children: ReactNode }> => {
    return ({ children }: { children: ReactNode }) => <ApiProvider>{children}</ApiProvider>;
  };

  it("should register QueryClient", () => {
    const { result } = renderHook(() => useQueryClient(), {
      wrapper: createWrapper(),
    });
    expect(result.current).toBeDefined();
  });
});
