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

import { IndexResources, Me } from "@scm-manager/ui-types";
import React, { createContext, FC, useContext } from "react";
import { QueryClient, useQueryClient } from "react-query";

export type BaseContext = {
  onIndexFetched?: (index: IndexResources) => void;
  onMeFetched?: (me: Me) => void;
};

export type LegacyContext = BaseContext & {
  initialize: () => void;
  queryClient?: QueryClient;
};

const Context = createContext<LegacyContext | undefined>(undefined);

export const useLegacyContext = () => {
  const context = useContext(Context);
  if (!context) {
    throw new Error("useLegacyContext can't be used outside of ApiProvider");
  }
  return context;
};

const createInitialContext = (queryClient: QueryClient, base: BaseContext): LegacyContext => {
  const ctx = {
    ...base,
    initialize: () => {
      if (ctx.onIndexFetched) {
        const index: IndexResources | undefined = queryClient.getQueryData("index");
        if (index) {
          ctx.onIndexFetched(index);
        }
      }
      if (ctx.onMeFetched) {
        const me: Me | undefined = queryClient.getQueryData("me");
        if (me) {
          ctx.onMeFetched(me);
        }
      }
    }
  };

  return ctx;
};

export const LegacyContextProvider: FC<BaseContext> = ({ onIndexFetched, onMeFetched, children }) => {
  const queryClient = useQueryClient();
  const ctx = createInitialContext(queryClient, { onIndexFetched, onMeFetched });

  return <Context.Provider value={ctx}>{children}</Context.Provider>;
};
