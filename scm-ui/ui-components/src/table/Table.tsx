import React, { FC, useState } from "react";
import styled from "styled-components";
import { Comparator } from "./types";
import SortIcon from "./SortIcon";

const StyledTable = styled.table.attrs(() => ({
  className: "table content is-hoverable"
}))``;

type Props = {
  data: any[];
  sortable?: boolean;
};

// @ts-ignore
const Table: FC<Props> = ({ data, sortable, children }) => {
  const [tableData, setTableData] = useState(data);
  const [ascending, setAscending] = useState(false);
  const [lastSortBy, setlastSortBy] = useState<number | undefined>();
  const [hoveredColumnIndex, setHoveredColumnIndex] = useState<number | undefined>();

  const isSortable = (child: any) => {
    return sortable && child.props.createComparator;
  };

  const sortFunctions: Comparator | undefined[] = [];
  React.Children.forEach(children, (child, index) => {
    if (isSortable(child)) {
      // @ts-ignore
      sortFunctions.push(child.props.createComparator(child.props, index));
    } else {
      sortFunctions.push(undefined);
    }
  });

  const mapDataToColumns = (row: any) => {
    return (
      <tr>
        {React.Children.map(children, (child, columnIndex) => {
          // @ts-ignore
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
    const sortableData = [...tableData];
    let sortOrder = ascending;
    if (lastSortBy !== index) {
      setAscending(true);
      sortOrder = true;
    }
    // @ts-ignore
    const sortFunction = sortOrder ? sortFunctions[index] : sortDescending(sortFunctions[index]);
    sortableData.sort(sortFunction);
    setTableData(sortableData);
    setAscending(!sortOrder);
    setlastSortBy(index);
  };

  // @ts-ignore
  return (
    tableData.length > 0 && (
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
                {isSortable(child) && renderSortIcon(child, ascending, index === lastSortBy || index === hoveredColumnIndex)}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>{tableData.map(mapDataToColumns)}</tbody>
      </StyledTable>
    )
  );
};

Table.defaultProps = {
  sortable: true
};

const renderSortIcon = (child: any, ascending: boolean, showIcon: boolean) => {
  if (child.props.ascendingIcon && child.props.descendingIcon) {
    return <SortIcon name={ascending ? child.props.ascendingIcon : child.props.descendingIcon} isVisible={showIcon} />;
  } else {
    return <SortIcon name={ascending ? "sort-amount-down-alt" : "sort-amount-down"} isVisible={showIcon} />;
  }
};

export default Table;
