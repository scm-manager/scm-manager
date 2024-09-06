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

import React, { FC, useContext, useMemo } from "react";
import { FieldValues, useFieldArray, UseFieldArrayReturn } from "react-hook-form";
import { ScmFormPathContextProvider, useScmFormPathContext } from "./FormPathContext";
import { useScmFormContext } from "./ScmFormContext";

type ContextType<T extends FieldValues = any> = UseFieldArrayReturn<T> & { isNested: boolean };

const ScmFormListContext = React.createContext<ContextType>(null as unknown as ContextType);

type Props = {
  name: string;
};

/**
 * Sets up an array field to be displayed in an enclosed *Form.Table* or *Form.List*.
 * A *Form.AddListEntryForm* can be used to implement a sub-form for adding new entries to the list.
 *
 * @beta
 * @since 2.43.0
 */
export const ScmFormListContextProvider: FC<Props> = ({ name, children }) => {
  const { control } = useScmFormContext();
  const prefix = useScmFormPathContext();
  const parentForm = useScmFormListContext();
  const nameWithPrefix = useMemo(() => (prefix ? `${prefix}.${name}` : name), [name, prefix]);
  const fieldArray = useFieldArray({
    control,
    name: nameWithPrefix,
  });
  const value = useMemo(() => ({ ...fieldArray, isNested: !!parentForm }), [fieldArray, parentForm]);

  return (
    <ScmFormPathContextProvider path={nameWithPrefix}>
      <ScmFormListContext.Provider value={value}>{children}</ScmFormListContext.Provider>
    </ScmFormPathContextProvider>
  );
};

export function useScmFormListContext() {
  return useContext(ScmFormListContext);
}
