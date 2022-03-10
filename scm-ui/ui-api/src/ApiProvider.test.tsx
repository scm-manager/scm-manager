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
