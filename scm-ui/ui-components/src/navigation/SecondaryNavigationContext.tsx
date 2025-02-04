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

import React, { ReactNode, useMemo, useState } from "react";

type SecondaryNavigationContextState = {
  collapsible: boolean;
  setCollapsible: (collapsed: boolean) => void;
};

const dummy: SecondaryNavigationContextState = {
  collapsible: false,
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  setCollapsible: () => {},
};

export const SecondaryNavigationContext = React.createContext<SecondaryNavigationContextState>(dummy);

export const SecondaryNavigationProvider = ({ children }: { children: ReactNode }) => {
  const [collapsible, setCollapsible] = useState(true);
  const contextValue = useMemo(() => ({ collapsible, setCollapsible }), [collapsible, setCollapsible]);

  return <SecondaryNavigationContext.Provider value={contextValue}>{children}</SecondaryNavigationContext.Provider>;
};
