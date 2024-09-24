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

import React, { FC, useContext, useState } from "react";

export type MenuContext = {
  isCollapsed: () => boolean;
  setCollapsed: (collapsed: boolean) => void;
};

/**
 * @deprecated
 */
export const MenuContext = React.createContext<MenuContext>({
  isCollapsed() {
    return false;
  },
  setCollapsed() {},
});

export const StateMenuContextProvider: FC = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);

  const context = {
    isCollapsed() {
      return collapsed;
    },
    setCollapsed,
  };

  return <MenuContext.Provider value={context}>{children}</MenuContext.Provider>;
};

const useMenuContext = () => {
  return useContext<MenuContext>(MenuContext);
};

export default useMenuContext;
