import { ReactNode } from "react";

export type Comparator = (a: any, b: any) => number;

export type ColumnProps = {
  header: ReactNode;
  row?: any;
  columnIndex?: number;
  createComparator?: (props: any, columnIndex: number) => Comparator;
  ascendingIcon?: string;
  descendingIcon?: string;
};
