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

import React, { FC, HTMLAttributes, useMemo } from "react";
import { useScmFormPathContext } from "../FormPathContext";
import { useScmFormContext } from "../ScmFormContext";
import { useWatch } from "react-hook-form";

type Props = {
  name: string;
  children?: (value: unknown) => React.ReactNode | React.ReactNode[];
} & HTMLAttributes<HTMLTableCellElement>;

/**
 * @beta
 * @since 2.43.0
 */
const ControlledColumn: FC<Props> = ({ name, children, ...props }) => {
  const { control } = useScmFormContext();
  const formPathPrefix = useScmFormPathContext();
  const nameWithPrefix = useMemo(() => (formPathPrefix ? `${formPathPrefix}.${name}` : name), [formPathPrefix, name]);
  const value = useWatch({ control, name: nameWithPrefix, disabled: typeof children === "function" });
  const allValues = useWatch({ control, name: formPathPrefix, disabled: typeof children !== "function" });

  return <td {...props}>{typeof children === "function" ? children(allValues) : value}</td>;
};

export default ControlledColumn;
