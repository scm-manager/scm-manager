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

import type { FC } from "react";
import React, { useContext, useMemo, useRef } from "react";

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
