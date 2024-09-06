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

import { createContext, useContext } from "react";
import defaultBinder from "./binder";

/**
 * The BinderContext should only be used to override the default binder for testing purposes.
 */
export const BinderContext = createContext(defaultBinder);

/**
 * Hook to get the binder from context.
 */
export const useBinder = () => {
  return useContext(BinderContext);
};

export default useBinder;
