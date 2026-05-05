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

import React, { createContext, FC, ReactNode, useContext } from "react";
import { Namespace } from "@scm-manager/ui-types";

export type NamespaceContext = {
  namespace: Namespace;
};

const Context = createContext<NamespaceContext | undefined>(undefined);

export const useNamespaceContext: () => NamespaceContext = () => {
  const context = useContext(Context);
  if (context === undefined) {
    throw new Error("useNamespaceContext must be used within a NamespaceContextProvider");
  }
  return context;
};

export const NamespaceContextProvider: FC<{ context: NamespaceContext; children: ReactNode }> = ({
  context,
  children,
}) => <Context.Provider value={context}>{children}</Context.Provider>;
