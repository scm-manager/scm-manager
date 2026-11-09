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

import React, { FC, isValidElement, ReactElement, useEffect, useState } from "react";
import classNames from "classnames";
import { Icon } from "../../buttons";
import { Notification } from "../../notifications";
import { ColumnProps } from "./Column";
import comparators, { Comparator } from "./comparators";
import TextColumn, { TextColumnProps } from "./TextColumn";

type Props = {
  data: any[];
  sortable?: boolean;
  emptyMessage?: string;
  children: ReactElement<ColumnProps> | Array<ReactElement<ColumnProps>>;
  className?: string;
};

// React 19 ignores defaultProps of function components, so the table supplies the defaults of text columns
const getColumnProps = (column: ReactElement<ColumnProps>): ColumnProps => {
  if (column.type === TextColumn) {
    const { dataKey } = column.props as TextColumnProps;
    return {
      createComparator: () => comparators.byKey(dataKey),
      ascendingIcon: "sort-alpha-down-alt",
      descendingIcon: "sort-alpha-down",
      ...column.props,
    };
  }
  return column.props;
};

const sortDescending = (sortAscending: Comparator): Comparator => {
  return (a, b) => sortAscending(a, b) * -1;
};

const getSortIconName = (columnProps: ColumnProps, ascending: boolean) => {
  if (columnProps.ascendingIcon && columnProps.descendingIcon) {
    return ascending ? columnProps.ascendingIcon : columnProps.descendingIcon;
  }
  return ascending ? "sort-amount-down-alt" : "sort-amount-down";
};

/**
 * Renders one row for each entry of `data` and one cell for each column given as child,
 * either a {@link Column} with a render function or a {@link TextColumn}.
 *
 * Columns with a `createComparator` can be sorted by clicking their header, text columns by default.
 *
 * @beta
 * @since 4.0.0
 */
const Table: FC<Props> = ({ data, sortable = true, children, emptyMessage, className }) => {
  const [tableData, setTableData] = useState(data);
  useEffect(() => {
    setTableData(data);
  }, [data]);
  const [ascending, setAscending] = useState(false);
  const [lastSortBy, setLastSortBy] = useState<number | undefined>();
  const [hoveredColumnIndex, setHoveredColumnIndex] = useState<number | undefined>();

  const columns = React.Children.toArray(children).filter(isValidElement) as Array<ReactElement<ColumnProps>>;
  const columnProps = columns.map(getColumnProps);
  const sortFunctions: Array<Comparator | undefined> = columnProps.map((column, index) =>
    sortable && column.createComparator ? column.createComparator(column, index) : undefined,
  );

  const tableSort = (index: number) => {
    const sortFn = sortFunctions[index];
    if (!sortFn) {
      throw new Error(`column with index ${index} is not sortable`);
    }
    const sortableData = [...tableData];
    let sortOrder = ascending;
    if (lastSortBy !== index) {
      setAscending(true);
      sortOrder = true;
    }
    const sortFunction = sortOrder ? sortFn : sortDescending(sortFn);
    sortableData.sort(sortFunction);
    setTableData(sortableData);
    setAscending(!sortOrder);
    setLastSortBy(index);
  };

  const shouldShowIcon = (index: number) => {
    return index === lastSortBy || index === hoveredColumnIndex;
  };

  if (!tableData || tableData.length <= 0) {
    if (emptyMessage) {
      return <Notification type="info">{emptyMessage}</Notification>;
    } else {
      return null;
    }
  }

  return (
    <table className={classNames("table content is-hoverable", className)}>
      <thead>
        <tr>
          {columnProps.map((column, index) => (
            <th
              className={sortFunctions[index] ? "is-clickable" : undefined}
              onClick={sortFunctions[index] ? () => tableSort(index) : undefined}
              onMouseEnter={() => setHoveredColumnIndex(index)}
              onMouseLeave={() => setHoveredColumnIndex(undefined)}
              key={index}
            >
              {column.header}
              {sortFunctions[index] && (
                <Icon className={classNames("ml-1", { "is-invisible": !shouldShowIcon(index) })}>
                  {getSortIconName(column, ascending)}
                </Icon>
              )}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {tableData.map((row, rowIndex) => (
          <tr key={rowIndex}>
            {columns.map((column, columnIndex) => (
              <td className={column.props.className} key={columnIndex}>
                {React.cloneElement(column, { columnIndex, rowIndex, row })}
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default Table;
