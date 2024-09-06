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

import React, { FC, ReactNode } from "react";
import { ColumnProps } from "./types";

type Props = ColumnProps & {
  children: (row: any, columnIndex: number, rowIndex: number) => ReactNode;
};

/**
 * @deprecated
 */
const Column: FC<Props> = ({ row, columnIndex, rowIndex, children }) => {
  if (row === undefined) {
    throw new Error("missing row, use column only as child of Table");
  }
  if (columnIndex === undefined) {
    throw new Error("missing row, use column only as child of Table");
  }
  if (rowIndex === undefined) {
    throw new Error("missing row, use column only as child of Table");
  }
  return <>{children(row, columnIndex, rowIndex)}</>;
};

export default Column;
