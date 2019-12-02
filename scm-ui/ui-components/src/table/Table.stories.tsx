import React from "react";
import { storiesOf } from "@storybook/react";
import Table from "./Table";
import Column from "./Column";
import TextColumn from "./TextColumn";
import { ColumnProps } from "./table";

storiesOf("Table|Table", module)
  .add("Default", () => (
    <Table data={[{ first: "dddd", second: "xyz" }, { first: "abc", second: "bbbb" }]}>
      <Column header={"FIRST"}>{(row: any) => <h1>{row.first}</h1>}</Column>
      <Column
        header={"SECOND"}
        createComparator={(props: ColumnProps, columnIndex: number) => {
          return (a: any, b: any) => {
            if (a.second > b.second) {
              return -1;
            } else if (a.second < b.second) {
              return 1;
            } else {
              return 0;
            }
          };
        }}
      >
        {(row: any) => <h2 style={{ color: "red" }}>{row.second}</h2>}
      </Column>
    </Table>
  ))
  .add("TextColumn", () => (
    <Table data={[{ first: "d", second: "y" }, { first: "a", second: "b" }, { first: "z", second: "a" }]}>
      <TextColumn header={"FIRST"} dataKey={"first"} />
      <TextColumn header={"SECOND"} dataKey={"second"} />
    </Table>
  ));
