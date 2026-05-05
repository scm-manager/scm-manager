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

import React, { FC, PropsWithChildren } from "react";
import { QueryClient, QueryClientProvider, focusManager } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { reset } from "./reset";

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
    },
  },
});

// This is needed to reload data on re-focusing the browser, not only on visibility change
// See https://tanstack.com/query/v4/docs/framework/react/guides/window-focus-refetching
focusManager.setEventListener((handleFocus) => {
  const onFocus = () => {
    handleFocus();
  };

  // Listen to visibilitychange and focus
  if (typeof window !== "undefined" && window.addEventListener) {
    window.addEventListener("visibilitychange", onFocus, false);
    window.addEventListener("focus", onFocus, false);
  }

  return () => {
    // Be sure to unsubscribe if a new handler is set
    window.removeEventListener("visibilitychange", onFocus);
    window.removeEventListener("focus", onFocus);
  };
});

type Props = PropsWithChildren & {
  devtools?: boolean;
};

const ApiProvider: FC<Props> = ({ devtools, children }) => {
  return (
    <QueryClientProvider client={queryClient}>
      {children}
      {devtools ? <ReactQueryDevtools initialIsOpen={false} /> : null}
    </QueryClientProvider>
  );
};

export const clearCache = () => {
  // we do a safe reset instead of clearing the whole cache
  // this should avoid missing link errors for index
  return reset(queryClient);
};

export default ApiProvider;
