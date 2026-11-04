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

import React, { ComponentProps, useState } from "react";
import type { Meta, StoryObj } from "@storybook/react";

import FilterInput from "./FilterInput";

const meta: Meta<typeof FilterInput> = {
  title: "Forms/FilterInput",
  component: FilterInput,
  decorators: [(Story) => <div style={{ margin: "2rem", maxWidth: "40rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

const FilterWithQuery = (props: Omit<ComponentProps<typeof FilterInput>, "filter">) => {
  const [query, setQuery] = useState(props.value ?? "");
  return (
    <>
      <FilterInput {...props} filter={setQuery} />
      <p className="mt-4">Current filter: {query}</p>
    </>
  );
};

export const Default: Story = {
  render: () => <FilterWithQuery />,
};

export const WithLabel: Story = {
  render: () => <FilterWithQuery label="Filter repositories" />,
};

export const WithInitialValue: Story = {
  render: () => <FilterWithQuery value="scm" />,
};
