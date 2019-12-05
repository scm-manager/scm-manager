import React, { FC } from "react";
import { ColumnProps } from "./types";
import comparators from "../comparators";

type Props = ColumnProps & {
  dataKey: string;
};

const TextColumn: FC<Props> = ({ row, dataKey }) => {
  return row[dataKey];
};

TextColumn.defaultProps = {
  createComparator: (props: Props) => {
    return comparators.byKey(props.dataKey);
  },
  ascendingIcon: "sort-alpha-down-alt",
  descendingIcon: "sort-alpha-down"
};

export default TextColumn;
