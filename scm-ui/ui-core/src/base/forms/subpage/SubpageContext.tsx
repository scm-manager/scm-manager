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

import React, { PropsWithChildren, useCallback, useContext, useMemo, useState } from "react";

type SubpageContextType = {
  activeSubpage: string | null;
  openSubpage: (name: string) => void;
  closeSubpage: () => void;
  saveActiveSubpage: () => Promise<void>;
  title: string;
  registerSubpage: (name: string, subpageTitle: string) => void;
  subpageTitles: Record<string, string>;
  error?: Error | null;
};

const SubpageContext = React.createContext<SubpageContextType>(null as never);

type ProviderProps = PropsWithChildren<{
  title: string;
  error?: Error | null;
  submitForm: () => Promise<boolean>;
  onLeaveSubpage?: () => void;
  onEnterSubpage?: () => void;
}>;

export function SubpageContextProvider({
  title,
  error,
  submitForm,
  onLeaveSubpage,
  onEnterSubpage,
  children,
}: ProviderProps) {
  const [activeSubpage, setActiveSubpage] = useState<string | null>(null);
  const [subpageTitles, setSubpageTitles] = useState<Record<string, string>>({});

  const openSubpage = useCallback(
    (name: string) => {
      onEnterSubpage?.();
      setActiveSubpage(name);
    },
    [onEnterSubpage],
  );

  const closeSubpage = useCallback(() => {
    onLeaveSubpage?.();
    setActiveSubpage(null);
  }, [onLeaveSubpage]);

  const saveActiveSubpage = useCallback(async () => {
    const success = await submitForm();
    if (success) {
      // The form values were already saved and reset; just return to the overview.
      setActiveSubpage(null);
    }
  }, [submitForm]);

  const registerSubpage = useCallback((name: string, subpageTitle: string) => {
    setSubpageTitles((prev) => (prev[name] === subpageTitle ? prev : { ...prev, [name]: subpageTitle }));
  }, []);

  const value = useMemo<SubpageContextType>(
    () => ({
      activeSubpage,
      openSubpage,
      closeSubpage,
      saveActiveSubpage,
      title,
      registerSubpage,
      subpageTitles,
      error,
    }),
    [activeSubpage, openSubpage, closeSubpage, saveActiveSubpage, title, registerSubpage, subpageTitles, error],
  );

  return <SubpageContext.Provider value={value}>{children}</SubpageContext.Provider>;
}

export function useSubpageContext() {
  const context = useContext(SubpageContext);
  if (!context) {
    throw new Error("useSubpageContext must be used within an SubpageForm");
  }
  return context;
}
