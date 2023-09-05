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
import React, { FC, ReactElement, useEffect, useState } from "react";
import { Comparator } from "./types";
import SortIcon from "./SortIcon";
import Notification from "../Notification";
import classNames from "classnames";

type Props = {
  data: any[];
  sortable?: boolean;
  emptyMessage?: string;
  children: Array<ReactElement>;
  className?: string;
};

/**
 * @deprecated
 */
const Table: FC<Props> = ({ data, sortable, children, emptyMessage, className }) => {
  const [tableData, setTableData] = useState(data);
  useEffect(() => {
    setTableData(data);
  }, [data]);
  const [ascending, setAscending] = useState(false);
  const [lastSortBy, setlastSortBy] = useState<number | undefined>();
  const [hoveredColumnIndex, setHoveredColumnIndex] = useState<number | undefined>();

  const isSortable = (child: ReactElement) => {
    return sortable && child.props.createComparator;
  };

  const sortFunctions: Comparator | undefined[] = [];
  React.Children.forEach(children, (child, index) => {
    if (child && isSortable(child as ReactElement)) {
      sortFunctions.push((child as ReactElement).props.createComparator((child as ReactElement).props, index));
    } else {
      sortFunctions.push(undefined);
    }
  });

  const mapDataToColumns = (row: any, rowIndex: number) => {
    return (
      <tr key={rowIndex}>
        {React.Children.map(children, (child, columnIndex) => {
          const { className: columnClassName, ...childProperties } = (child as ReactElement).props;
          return (
            <td className={columnClassName}>
              {React.cloneElement(child as ReactElement, { ...childProperties, columnIndex, rowIndex, row })}
            </td>
          );
        })}
      </tr>
    );
  };

  const sortDescending = (sortAscending: (a: any, b: any) => number) => {
    return (a: any, b: any) => {
      return sortAscending(a, b) * -1;
    };
  };

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
    setlastSortBy(index);
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
          {React.Children.map(children, (child, index) => (
            <th
              className={isSortable(child as ReactElement) && "is-clickable"}
              onClick={isSortable(child as ReactElement) ? () => tableSort(index) : undefined}
              onMouseEnter={() => setHoveredColumnIndex(index)}
              onMouseLeave={() => setHoveredColumnIndex(undefined)}
              key={index}
            >
              {(child as ReactElement).props.header}
              {isSortable(child as ReactElement) &&
                renderSortIcon(child as ReactElement, ascending, shouldShowIcon(index))}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>{tableData.map(mapDataToColumns)}</tbody>
    </table>
  );
};

Table.defaultProps = {
  sortable: true,
};

const renderSortIcon = (child: ReactElement, ascending: boolean, showIcon: boolean) => {
  if (child.props.ascendingIcon && child.props.descendingIcon) {
    return <SortIcon name={ascending ? child.props.ascendingIcon : child.props.descendingIcon} isVisible={showIcon} />;
  } else {
    return <SortIcon name={ascending ? "sort-amount-down-alt" : "sort-amount-down"} isVisible={showIcon} />;
  }
};

export default Table;
