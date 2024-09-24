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

import React, { createContext, FC, useContext, useState } from "react";

export type NamespaceAndNameContext = {
  namespace?: string;
  setNamespace: (namespace: string) => void;
  name?: string;
  setName: (name: string) => void;
};
const Context = createContext<NamespaceAndNameContext | undefined>(undefined);

export const useNamespaceAndNameContext = () => {
  const context = useContext(Context);
  if (!context) {
    throw new Error("useNamespaceAndNameContext can't be used outside of ApiProvider");
  }
  return context;
};

export const NamespaceAndNameContextProvider: FC = ({ children }) => {
  const [namespace, setNamespace] = useState("");
  const [name, setName] = useState("");

  return <Context.Provider value={{ namespace, setNamespace, name, setName }}>{children}</Context.Provider>;
};
