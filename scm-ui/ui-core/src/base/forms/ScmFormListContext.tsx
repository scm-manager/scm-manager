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
