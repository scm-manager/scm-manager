import React from "react";
import { storiesOf } from "@storybook/react";
import Table from "./Table";
import Column from "./Column";
import TextColumn from "./TextColumn";

storiesOf("Table|Table", module)
  .add("Default", () => (
    <Table data={[{ first: "a", second: "b" }, { first: "d", second: "y" }]}>
      <Column header={"FIRST"}>{(row: any) => <h1>{row.first}</h1>}</Column>
      <Column header={"SECOND"}>{(row: any) => <h2 style={{ color: "red" }}>{row.second}</h2>}</Column>
    </Table>
  ))
  .add("TextColumn", () => (
    <Table data={[{ first: "d", second: "y" }, { first: "a", second: "b" }, { first: "z", second: "a" }]}>
      <TextColumn header={"FIRST"} dataKey={"first"} />
      <TextColumn header={"SECOND"} dataKey={"second"} />
    </Table>
  ));
