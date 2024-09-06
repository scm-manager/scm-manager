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
