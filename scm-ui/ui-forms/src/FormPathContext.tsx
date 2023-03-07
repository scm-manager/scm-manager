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

const ScmFormPathContext = React.createContext<string>("");

export function useScmFormPathContext() {
  return useContext(ScmFormPathContext);
}

export const ScmFormPathContextProvider: FC<{ path: string }> = ({ children, path }) => (
  <ScmFormPathContext.Provider value={path}>{children}</ScmFormPathContext.Provider>
);

/**
 * This component removes redundancy by declaring a prefix that is shared by all enclosed form-related components.
 * It might be helpful when the data structure of a list's items does not correspond with the individual list item's
 * form structure.
 *
 * @beta
 * @since 2.43.0
 * @example ```
 * // For data of structure
 *    {
 *       subForm: { foo: boolean, bar: string };
 *       flag: boolean;
 *       tag: string;
 *    }
 * // Because we use ConfigurationForm we get and update the whole object.
 * // We only want to create a form for 'subForm' without having to repeat it for every subField
 * // while keeping the original data structure for the whole form. *
 *
 * // Using this component we can write:
 * <Form.PathContext path="subForm">
 *   <Form.Input name="foo" />
 *   <Form.Checkbox name="bar" />
 * </Form.PathContext>
 *
 * // Instead of
 *
 * <Form.Input name="subForm.foo" />
 * <Form.Checkbox name="subForm.bar" />
 *
 * // This pattern becomes useful in complex or large forms.
 * ```
 */
export const ScmNestedFormPathContextProvider: FC<{ path: string }> = ({ children, path }) => {
  const prefix = useScmFormPathContext();
  const pathWithPrefix = useMemo(() => (prefix ? `${prefix}.${path}` : path), [path, prefix]);
  return <ScmFormPathContext.Provider value={pathWithPrefix}>{children}</ScmFormPathContext.Provider>;
};
