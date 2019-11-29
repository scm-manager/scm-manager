import React, { FC, ReactNode } from "react";
import { ColumnProps } from "./types";

type Props = ColumnProps & {
  children: (row: any) => ReactNode;
};

const Column: FC<Props> = ({ row, children }) => {
  return <>{children(row)}</>;
};

export default Column;
