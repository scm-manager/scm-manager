import React, { FC, useState } from "react";
import styled from "styled-components";
import { SortTypes } from "./types";
import Icon from "../Icon";

const StyledTable = styled.table.attrs(() => ({
  className: "table content is-hoverable"
}))``;

const IconWithMarginLeft = styled(Icon)`
  margin-left: 0.25em;
`;

type SortableTableProps = {
  data: any[];
};

// @ts-ignore
const Table: FC<SortableTableProps> = ({ data, children }) => {
  const [tableData, setTableData] = useState(data);
  const [ascending, setAscending] = useState(false);
  const [lastSortBy, setlastSortBy] = useState(0);

  // @ts-ignore
  const sortFunctions = React.Children.map(children, child =>
    // @ts-ignore
    child.props.createComparator ? child.props.createComparator(child.props) : undefined
  );

  const mapDataToColumns = (row: any) => {
    return (
      <tr>
        {React.Children.map(children, child => {
          // @ts-ignore
          return <td>{React.cloneElement(child, { ...child.props, row })}</td>;
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
    const sortFunction = sortOrder ? sortFunctions[index] : sortDescending(sortFunctions[index]);
    sortableData.sort(sortFunction);
    setTableData(sortableData);
    setAscending(!sortOrder);
    setlastSortBy(index);
  };

  return (
    tableData.length > 0 && (
      <StyledTable>
        <thead>
          <tr>
            {React.Children.map(children, (child, index) => (
              // @ts-ignore
              <th
                className={child.props.createComparator && "has-cursor-pointer"}
                onClick={child.props.createComparator ? () => tableSort(index) : undefined}
              >
                {child.props.header}

                {child.props.createComparator && renderSortIcon(child.props.sortType, ascending)}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>{tableData.map(mapDataToColumns)}</tbody>
      </StyledTable>
    )
  );
};

const renderSortIcon = (contentType: string, ascending: boolean) => {
  if (contentType === SortTypes.Text) {
    return <IconWithMarginLeft name={ascending ? "sort-alpha-down-alt" : "sort-alpha-down"} />;
  } else {
    return <IconWithMarginLeft name={ascending ? "sort-amount-down-alt" : "sort-amount-down"} />;
  }
};

export default Table;
