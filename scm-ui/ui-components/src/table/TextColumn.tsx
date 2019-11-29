import React, {FC} from "react";
import {ColumnProps, SortTypes} from "./types";

type Props = ColumnProps & {
  dataKey: string;
};

const TextColumn: FC<Props> = ({ row, dataKey }) => {
  return row[dataKey];
};

TextColumn.defaultProps = {
  createComparator: (props: Props) => {
    return (a: any, b: any) => {
      if (a[props.dataKey] < b[props.dataKey]) {
        return -1;
      } else if (a[props.dataKey] > b[props.dataKey]) {
        return 1;
      } else {
        return 0;
      }
    };
  },
  sortType: SortTypes.Text
};

export default TextColumn;
