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
import React, { FC, useEffect } from "react";
import { QueryClient, QueryClientProvider } from "react-query";
import { ReactQueryDevtools } from "react-query/devtools";
import { LegacyContext, LegacyContextProvider } from "./LegacyContext";
import { IndexResources, Me } from "@scm-manager/ui-types";
import { reset } from "./reset";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false
    }
  }
});

type Props = LegacyContext & {
  index?: IndexResources;
  me?: Me;
  devtools?: boolean;
};

const ApiProvider: FC<Props> = ({ children, index, me, onMeFetched, onIndexFetched, devtools = true }) => {
  useEffect(() => {
    if (index) {
      queryClient.setQueryData("index", index);
      if (onIndexFetched) {
        onIndexFetched(index);
      }
    }
  }, [index, onIndexFetched]);
  useEffect(() => {
    if (me) {
      queryClient.setQueryData("me", me);
      if (onMeFetched) {
        onMeFetched(me);
      }
    }
  }, [me, onMeFetched]);
  return (
    <QueryClientProvider client={queryClient}>
      <LegacyContextProvider onIndexFetched={onIndexFetched} onMeFetched={onMeFetched}>
        {children}
      </LegacyContextProvider>
      {devtools ? <ReactQueryDevtools initialIsOpen={false} /> : null}
    </QueryClientProvider>
  );
};

export { Props as ApiProviderProps };

export const clearCache = () => {
  // we do a safe reset instead of clearing the whole cache
  // this should avoid missing link errors for index
  return reset(queryClient);
};

export default ApiProvider;
