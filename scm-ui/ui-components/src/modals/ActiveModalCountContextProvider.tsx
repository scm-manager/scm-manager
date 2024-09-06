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

import ActiveModalCountContext from "./activeModalCountContext";
import React, { FC, useCallback, useState } from "react";

/**
 * A simple counter that allows developers to keep track of how many modals are currently open.
 *
 * A default provider instance wraps the SCM-Manager so there is no need for plugins to use this component.
 *
 * Required by {@link useActiveModals}.
 */
const ActiveModalCountContextProvider: FC = ({ children }) => {
  const [activeModalCount, setActiveModalCount] = useState(0);
  const incrementModalCount = useCallback(() => setActiveModalCount((prev) => prev + 1), []);
  const decrementModalCount = useCallback(() => setActiveModalCount((prev) => prev - 1), []);

  return (
    <ActiveModalCountContext.Provider
      value={{ value: activeModalCount, increment: incrementModalCount, decrement: decrementModalCount }}
    >
      {children}
    </ActiveModalCountContext.Provider>
  );
};

export default ActiveModalCountContextProvider;
