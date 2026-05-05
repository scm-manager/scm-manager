/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

// import React from "react";
// import { storiesOf } from "@storybook/react";
// import Table from "./Table";
// import Column from "./Column";
// import TextColumn from "./TextColumn";
// import styled from "styled-components";
//
// const StyledTable = styled(Table)`
//   width: 400px;
//   border: 1px dashed black;
//   padding: 4px;
//   margin: 4px;
//   td {
//     word-break: break-word;
//   }
// `;
//
// storiesOf("Table", module)
//   .add("Default", () => (
//     <Table
//       data={[
//         { firstname: "Tricia", lastname: "McMillan", email: "tricia@hitchhiker.com" },
//         { firstname: "Arthur", lastname: "Dent", email: "arthur@hitchhiker.com" },
//       ]}
//     >
//       <Column header="First Name">{(row: any) => <h4>{row.firstname}</h4>}</Column>
//       <Column
//         className="has-background-success"
//         header="Last Name"
//         createComparator={() => {
//           return (a: any, b: any) => {
//             if (a.lastname > b.lastname) {
//               return -1;
//             } else if (a.lastname < b.lastname) {
//               return 1;
//             } else {
//               return 0;
//             }
//           };
//         }}
//       >
//         {(row: any) => <b className="has-text-danger">{row.lastname}</b>}
//       </Column>
//       <Column header="E-Mail">{(row: any) => <span>{row.email}</span>}</Column>
//     </Table>
//   ))
//   .add("TextColumn", () => (
//     <Table
//       data={[
//         { id: "21", title: "Pommes", desc: "Fried potato sticks" },
//         { id: "42", title: "Quarter-Pounder", desc: "Big burger" },
//         { id: "-84", title: "Icecream", desc: "Cold dessert" },
//       ]}
//     >
//       <TextColumn header="Id" dataKey="id" />
//       <TextColumn header="Name" dataKey="title" />
//       <TextColumn header="Description" dataKey="desc" />
//     </Table>
//   ))
//   .add("Empty", () => (
//     <Table data={[]} emptyMessage="No data found.">
//       <TextColumn header="Id" dataKey="id" />
//       <TextColumn header="Name" dataKey="name" />
//     </Table>
//   ))
//   .add("Table with Word-Break", () => (
//     <StyledTable
//       data={[
//         {
//           id: "42",
//           name: "herp_derp_schlerp_ferp_gerp_nerp_terp_ierp_perp_lerp_merp_oerp_zerp_serp_verp_herp",
//         },
//         {
//           id: "17",
//           name: "herp_derp_schlerp_ferp_gerp_nerp_terp_ierp_perp_lerp_merp_oerp_zerp_serp_verp",
//         },
//       ]}
//       emptyMessage="No data found."
//     >
//       <TextColumn header="Id" dataKey="id" />
//       <TextColumn header="Name" dataKey="name" />
//     </StyledTable>
//   ));

import React from "react";
import styled from "styled-components";
import type { Meta, StoryObj } from "@storybook/react";

import Table from "./Table";
import Column from "./Column";
import TextColumn from "./TextColumn";

// --- Helper Components & Mock Data (preserved from your original story) ---

const StyledTable = styled(Table)`
  width: 400px;
  border: 1px dashed black;
  padding: 4px;
  margin: 4px;
  td {
    word-break: break-word;
  }
`;

const tableData = [
  { firstname: "Tricia", lastname: "McMillan", email: "tricia@hitchhiker.com" },
  { firstname: "Arthur", lastname: "Dent", email: "arthur@hitchhiker.com" },
];

const textColumnData = [
  { id: "21", title: "Pommes", desc: "Fried potato sticks" },
  { id: "42", title: "Quarter-Pounder", desc: "Big burger" },
  { id: "-84", title: "Icecream", desc: "Cold dessert" },
];

const wordBreakData = [
  { id: "42", name: "herp_derp_schlerp_ferp_gerp_nerp_terp_ierp_perp_lerp_merp_oerp_zerp_serp_verp_herp" },
  { id: "17", name: "herp_derp_schlerp_ferp_gerp_nerp_terp_ierp_perp_lerp_merp_oerp_zerp_serp_verp" },
];

// --- Storybook Metadata ---

const meta: Meta<typeof Table> = {
  title: "Components/Table",
  component: Table,
  decorators: [(Story) => <div style={{ margin: "2rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

// --- Story Definitions ---

type Story = StoryObj<typeof meta>;

// For stories with complex children or custom components, we use a `render` function.
export const Default: Story = {
  render: () => (
    <Table data={tableData}>
      <Column header="First Name">{(row: any) => <h4>{row.firstname}</h4>}</Column>
      <Column
        className="has-background-success"
        header="Last Name"
        createComparator={() => (a: any, b: any) => b.lastname.localeCompare(a.lastname)}
      >
        {(row: any) => <b className="has-text-danger">{row.lastname}</b>}
      </Column>
      <Column header="E-Mail">{(row: any) => <span>{row.email}</span>}</Column>
    </Table>
  ),
};

export const WithTextColumn: Story = {
  name: "With TextColumn",
  render: () => (
    <Table data={textColumnData}>
      <TextColumn header="Id" dataKey="id" />
      <TextColumn header="Name" dataKey="title" />
      <TextColumn header="Description" dataKey="desc" />
    </Table>
  ),
};

export const Empty: Story = {
  render: () => (
    <Table data={[]} emptyMessage="No data found.">
      <TextColumn header="Id" dataKey="id" />
      <TextColumn header="Name" dataKey="name" />
    </Table>
  ),
};

export const WithWordBreak: Story = {
  name: "With Word-Break",
  render: () => (
    <StyledTable data={wordBreakData} emptyMessage="No data found.">
      <TextColumn header="Id" dataKey="id" />
      <TextColumn header="Name" dataKey="name" />
    </StyledTable>
  ),
};
