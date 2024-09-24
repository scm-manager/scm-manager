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
