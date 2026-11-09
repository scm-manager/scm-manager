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

import React from "react";
import { fireEvent, render, screen, within } from "@testing-library/react";
import Table from "./Table";
import Column from "./Column";
import TextColumn from "./TextColumn";
import comparators from "./comparators";

const data = [
  { name: "Trillian", age: 30 },
  { name: "Arthur", age: 42 },
  { name: "Zaphod", age: 39 },
];

const columnTexts = (columnIndex: number) =>
  screen
    .getAllByRole("row")
    .slice(1)
    .map((row) => within(row).getAllByRole("cell")[columnIndex].textContent);

describe("Table", () => {
  it("should render a header and a row for each entry", () => {
    render(
      <Table data={data}>
        <TextColumn header="Name" dataKey="name" />
        <Column header="Age">{(row) => `${row.age} years`}</Column>
      </Table>,
    );

    expect(screen.getAllByRole("columnheader").map((header) => header.textContent)).toEqual(["Name", "Age"]);
    expect(columnTexts(0)).toEqual(["Trillian", "Arthur", "Zaphod"]);
    expect(columnTexts(1)).toEqual(["30 years", "42 years", "39 years"]);
  });

  it("should pass row, column index and row index to the render function", () => {
    render(
      <Table data={data}>
        <TextColumn header="Name" dataKey="name" />
        <Column header="Position">{(row, columnIndex, rowIndex) => `${row.name} ${columnIndex} ${rowIndex}`}</Column>
      </Table>,
    );

    expect(columnTexts(1)).toEqual(["Trillian 1 0", "Arthur 1 1", "Zaphod 1 2"]);
  });

  it("should set the class name of a column on its cells", () => {
    render(
      <Table data={data}>
        <TextColumn header="Name" dataKey="name" className="has-text-right" />
      </Table>,
    );

    screen.getAllByRole("cell").forEach((cell) => expect(cell.classList.contains("has-text-right")).toBe(true));
  });

  it("should render the empty message without data", () => {
    render(
      <Table data={[]} emptyMessage="No data found">
        <TextColumn header="Name" dataKey="name" />
      </Table>,
    );

    expect(screen.getByText("No data found")).toBeTruthy();
    expect(screen.queryByRole("table")).toBeNull();
  });

  it("should render nothing without data and empty message", () => {
    const { container } = render(
      <Table data={[]}>
        <TextColumn header="Name" dataKey="name" />
      </Table>,
    );

    expect(container.innerHTML).toBe("");
  });

  it("should sort a column with comparator ascending and then descending", () => {
    render(
      <Table data={data}>
        <TextColumn header="Name" dataKey="name" />
        <Column header="Age" createComparator={() => comparators.byKey("age")}>
          {(row) => row.age}
        </Column>
      </Table>,
    );

    fireEvent.click(screen.getByRole("columnheader", { name: "Age" }));
    expect(columnTexts(1)).toEqual(["30", "39", "42"]);

    fireEvent.click(screen.getByRole("columnheader", { name: "Age" }));
    expect(columnTexts(1)).toEqual(["42", "39", "30"]);
  });

  it("should sort a text column by its data key by default", () => {
    render(
      <Table data={data}>
        <TextColumn header="Name" dataKey="name" />
      </Table>,
    );

    const header = screen.getByRole("columnheader", { name: "Name" });
    expect(header.classList.contains("is-clickable")).toBe(true);
    expect(header.querySelector(".fa-sort-alpha-down, .fa-sort-alpha-down-alt")).not.toBeNull();

    fireEvent.click(header);
    expect(columnTexts(0)).toEqual(["Arthur", "Trillian", "Zaphod"]);
  });

  it("should use the comparator of a text column instead of the default", () => {
    render(
      <Table data={data}>
        <TextColumn header="Name" dataKey="name" createComparator={() => comparators.byKey("age")} />
      </Table>,
    );

    fireEvent.click(screen.getByRole("columnheader", { name: "Name" }));
    expect(columnTexts(0)).toEqual(["Trillian", "Zaphod", "Arthur"]);
  });

  it("should use the default sort icons for columns without own icons", () => {
    render(
      <Table data={data}>
        <Column header="Age" createComparator={() => comparators.byKey("age")}>
          {(row) => row.age}
        </Column>
      </Table>,
    );

    const header = screen.getByRole("columnheader", { name: "Age" });
    expect(header.querySelector(".fa-sort-amount-down, .fa-sort-amount-down-alt")).not.toBeNull();
  });

  it("should not sort columns without comparator", () => {
    render(
      <Table data={data}>
        <Column header="Name">{(row) => row.name}</Column>
      </Table>,
    );

    const header = screen.getByRole("columnheader", { name: "Name" });
    expect(header.classList.contains("is-clickable")).toBe(false);

    fireEvent.click(header);
    expect(columnTexts(0)).toEqual(["Trillian", "Arthur", "Zaphod"]);
  });

  it("should not sort if the table is not sortable", () => {
    render(
      <Table data={data} sortable={false}>
        <TextColumn header="Name" dataKey="name" />
      </Table>,
    );

    const header = screen.getByRole("columnheader", { name: "Name" });
    expect(header.querySelector(".icon")).toBeNull();

    fireEvent.click(header);
    expect(columnTexts(0)).toEqual(["Trillian", "Arthur", "Zaphod"]);
  });

  it("should show the new data if the data changes", () => {
    const { rerender } = render(
      <Table data={data}>
        <TextColumn header="Name" dataKey="name" />
      </Table>,
    );

    rerender(
      <Table data={[{ name: "Marvin" }]}>
        <TextColumn header="Name" dataKey="name" />
      </Table>,
    );

    expect(columnTexts(0)).toEqual(["Marvin"]);
  });
});
