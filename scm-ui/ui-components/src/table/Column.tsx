import React, { FC, ReactNode } from "react";
import { ColumnProps } from "./types";

type Props = ColumnProps & {
  children: (row: any, columnIndex: number) => ReactNode;
};

const Column: FC<Props> = ({ row, columnIndex, children }) => {
  if (row === undefined) {
    throw new Error("missing row, use column only as child of Table");
  }
  if (columnIndex === undefined) {
    throw new Error("missing row, use column only as child of Table");
  }
  return <>{children(row, columnIndex)}</>;
};

export default Column;
