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

import React, { createContext, FC, useContext, useState } from "react";

type NamespaceAndNameContext = {
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
