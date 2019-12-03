import React from "react";
import { storiesOf } from "@storybook/react";
import Table from "./Table";
import Column from "./Column";
import TextColumn from "./TextColumn";
import { ColumnProps } from "./table";

storiesOf("Table|Table", module)
  .add("Default", () => (
    <Table data={[{ firstname: "Tricia", lastname: "McMillan", email: "tricia@hitchhiker.com" }, { firstname: "Arthur", lastname: "Dent", email: "arthur@hitchhiker.com" }]}>
      <Column header={"First Name"}>{(row: any) => <h4>{row.firstname}</h4>}</Column>
      <Column
        header={"Last Name"}
        createComparator={(props: ColumnProps, columnIndex: number) => {
          return (a: any, b: any) => {
            if (a.lastname > b.lastname) {
              return -1;
            } else if (a.lastname < b.lastname) {
              return 1;
            } else {
              return 0;
            }
          };
        }}
      >
        {(row: any) => <b style={{ color: "red" }}>{row.lastname}</b>}
      </Column>
      <Column header={"E-Mail"}>{(row: any) => <a>{row.email}</a>}</Column>
    </Table>
  ))
  .add("TextColumn", () => (
    <Table data={[{ id: "21", title: "Pommes", desc: "Fried potato sticks" },{ id: "42", title: "Quarter-Pounder", desc: "Big burger" }, {id: "-84", title: "Icecream", desc: "Cold dessert"}]}>
      <TextColumn header={"Id"} dataKey={"id"} />
      <TextColumn header={"Name"} dataKey={"title"} />
      <TextColumn header={"Description"} dataKey={"desc"} />
    </Table>
  ));
