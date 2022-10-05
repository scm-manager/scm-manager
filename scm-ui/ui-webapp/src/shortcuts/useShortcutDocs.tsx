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

import React, { FC, useContext, useMemo, useRef } from "react";

export type ShortcutDocsContextType = {
  docs: Readonly<Record<string, string>>;
  add: (key: string, description: string) => void;
  remove: (key: string) => void;
};

const ShortcutDocsContext = React.createContext<ShortcutDocsContextType>({} as ShortcutDocsContextType);

export const ShortcutDocsContextProvider: FC = ({ children }) => {
  const docs = useRef<Record<string, string>>({});
  const value = useMemo(
    () => ({
      docs: docs.current,
      add: (key: string, description: string) => (docs.current[key] = description),
      remove: (key: string) => {
        delete docs.current[key];
      },
    }),
    []
  );

  return <ShortcutDocsContext.Provider value={value}>{children}</ShortcutDocsContext.Provider>;
};

export default function useShortcutDocs() {
  return useContext(ShortcutDocsContext);
}
