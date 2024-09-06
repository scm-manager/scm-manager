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

import React, { PropsWithChildren, useContext } from "react";
import { UseFormReturn } from "react-hook-form";
import type { TFunction } from "i18next";

type ContextType<T = any> = UseFormReturn<T> & {
  t: TFunction;
  readOnly?: boolean;
  formId: string;
};

const ScmFormContext = React.createContext<ContextType>(null as unknown as ContextType);

export function ScmFormContextProvider<T>({ children, ...props }: PropsWithChildren<ContextType<T>>) {
  return <ScmFormContext.Provider value={props}>{children}</ScmFormContext.Provider>;
}

export function useScmFormContext() {
  return useContext(ScmFormContext);
}
