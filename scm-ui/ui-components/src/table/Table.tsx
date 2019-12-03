import React, { FC, ReactElement, useState } from "react";
import styled from "styled-components";
import { Comparator } from "./table";
import SortIcon from "./SortIcon";
import Notification from "../Notification";

const StyledTable = styled.table.attrs(() => ({
  className: "table content is-hoverable"
}))``;

type Props = {
  data: any[];
  sortable?: boolean;
  emptyMessage?: string;
  children: Array<ReactElement>;
};

const Table: FC<Props> = ({ data, sortable, children, emptyMessage }) => {
  const [tableData, setTableData] = useState(data);
  const [ascending, setAscending] = useState(false);
  const [lastSortBy, setlastSortBy] = useState<number | undefined>();
  const [hoveredColumnIndex, setHoveredColumnIndex] = useState<number | undefined>();

  const isSortable = (child: ReactElement) => {
    return sortable && child.props.createComparator;
  };

  const sortFunctions: Comparator | undefined[] = [];
  React.Children.forEach(children, (child, index) => {
    if (child && isSortable(child)) {
      sortFunctions.push(child.props.createComparator(child.props, index));
    } else {
      sortFunctions.push(undefined);
    }
  });

  const mapDataToColumns = (row: any) => {
    return (
      <tr>
        {React.Children.map(children, (child, columnIndex) => {
          return <td>{React.cloneElement(child, { ...child.props, columnIndex, row })}</td>;
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
    <StyledTable>
      <thead>
        <tr>
          {React.Children.map(children, (child, index) => (
            <th
              className={isSortable(child) && "has-cursor-pointer"}
              onClick={isSortable(child) ? () => tableSort(index) : undefined}
              onMouseEnter={() => setHoveredColumnIndex(index)}
              onMouseLeave={() => setHoveredColumnIndex(undefined)}
            >
              {child.props.header}
              {isSortable(child) && renderSortIcon(child, ascending, shouldShowIcon(index))}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>{tableData.map(mapDataToColumns)}</tbody>
    </StyledTable>
  );
};

Table.defaultProps = {
  sortable: true
};

const renderSortIcon = (child: ReactElement, ascending: boolean, showIcon: boolean) => {
  if (child.props.ascendingIcon && child.props.descendingIcon) {
    return <SortIcon name={ascending ? child.props.ascendingIcon : child.props.descendingIcon} isVisible={showIcon} />;
  } else {
    return <SortIcon name={ascending ? "sort-amount-down-alt" : "sort-amount-down"} isVisible={showIcon} />;
  }
};

export default Table;
