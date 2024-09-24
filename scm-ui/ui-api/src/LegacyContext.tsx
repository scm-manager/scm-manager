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

import { IndexResources, Me } from "@scm-manager/ui-types";
import React, { createContext, FC, useContext } from "react";
import { QueryClient, useQueryClient } from "react-query";

export type BaseContext = {
  onIndexFetched?: (index: IndexResources) => void;
  onMeFetched?: (me: Me) => void;
};

export type LegacyContext = BaseContext & {
  initialize: () => void;
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
