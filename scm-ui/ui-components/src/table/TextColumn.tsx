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

import React, { FC } from "react";
import { ColumnProps } from "./types";
import comparators from "../comparators";

type Props = ColumnProps & {
  dataKey: string;
};

/**
 * @deprecated
 */
const TextColumn: FC<Props> = ({ row, dataKey }) => {
  return row[dataKey];
};

TextColumn.defaultProps = {
  createComparator: (props: Props) => {
    return comparators.byKey(props.dataKey);
  },
  ascendingIcon: "sort-alpha-down-alt",
  descendingIcon: "sort-alpha-down",
};

export default TextColumn;
