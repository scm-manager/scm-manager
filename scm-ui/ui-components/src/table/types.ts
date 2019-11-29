import { ReactNode } from "react";

export type Comparator = (a: any, b: any) => number;

export type ColumnProps = {
  header: ReactNode;
  row?: any;
  createComparator?: (props: any) => Comparator;
};
