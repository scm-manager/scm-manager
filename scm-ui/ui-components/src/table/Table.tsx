import React, { FC, useState } from "react";

type SortableTableProps = {
  data: any[];
};

// @ts-ignore
const Table: FC<SortableTableProps> = ({ data, children }) => {
  const [tableData, setTableData] = useState(data);
  const [ascending, setAscending] = useState(true);
  const [lastSortBy, setlastSortBy] = useState(0);

  // @ts-ignore
  const sortFunctions = React.Children.map(children, child => child.props.createComparator(child.props));

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
      <table className="text-left">
        <thead>
          <tr>
            {React.Children.map(children, (child, index) => (
              // @ts-ignore
              <th onClick={() => tableSort(index)}>{child.props.header}</th>
            ))}
          </tr>
        </thead>
        <tbody>{tableData.map(mapDataToColumns)}</tbody>
      </table>
    )
  );
};

export default Table;
