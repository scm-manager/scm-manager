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

import { FC } from "react";
import { ColumnProps } from "./Column";

export type TextColumnProps = ColumnProps & {
  dataKey: string;
};

/**
 * Column of a {@link Table}, which renders the value of `dataKey` of each row.
 *
 * The {@link Table} sorts it by that value unless `createComparator` is given.
 *
 * @beta
 * @since 4.0.0
 */
const TextColumn: FC<TextColumnProps> = ({ row, dataKey }) => {
  return row[dataKey];
};

export default TextColumn;
