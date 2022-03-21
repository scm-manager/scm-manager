/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { storiesOf } from "@storybook/react";
import Table from "./Table";
import Column from "./Column";
import TextColumn from "./TextColumn";
import styled from "styled-components";

const StyledTable = styled(Table)`
  width: 400px;
  border: 1px dashed black;
  padding: 4px;
  margin: 4px;
  td {
    word-break: break-word;
  }
`;

storiesOf("Table", module)
  .add("Default", () => (
    <Table
      data={[
        { firstname: "Tricia", lastname: "McMillan", email: "tricia@hitchhiker.com" },
        { firstname: "Arthur", lastname: "Dent", email: "arthur@hitchhiker.com" },
      ]}
    >
      <Column header={"First Name"}>{(row: any) => <h4>{row.firstname}</h4>}</Column>
      <Column
        className="has-background-success"
        header={"Last Name"}
        createComparator={() => {
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
        {(row: any) => <b className="has-text-danger">{row.lastname}</b>}
      </Column>
      <Column header={"E-Mail"}>{(row: any) => <span>{row.email}</span>}</Column>
    </Table>
  ))
  .add("TextColumn", () => (
    <Table
      data={[
        { id: "21", title: "Pommes", desc: "Fried potato sticks" },
        { id: "42", title: "Quarter-Pounder", desc: "Big burger" },
        { id: "-84", title: "Icecream", desc: "Cold dessert" },
      ]}
    >
      <TextColumn header="Id" dataKey="id" />
      <TextColumn header="Name" dataKey="title" />
      <TextColumn header="Description" dataKey="desc" />
    </Table>
  ))
  .add("Empty", () => (
    <Table data={[]} emptyMessage="No data found.">
      <TextColumn header="Id" dataKey="id" />
      <TextColumn header="Name" dataKey="name" />
    </Table>
  ))
  .add("Table with Word-Break", () => (
    <StyledTable
      data={[
        {
          id: "42",
          name: "herp_derp_schlerp_ferp_gerp_nerp_terp_ierp_perp_lerp_merp_oerp_zerp_serp_verp_herp",
        },
        {
          id: "17",
          name: "herp_derp_schlerp_ferp_gerp_nerp_terp_ierp_perp_lerp_merp_oerp_zerp_serp_verp",
        },
      ]}
      emptyMessage="No data found."
    >
      <TextColumn header="Id" dataKey="id" />
      <TextColumn header="Name" dataKey="name" />
    </StyledTable>
  ));
