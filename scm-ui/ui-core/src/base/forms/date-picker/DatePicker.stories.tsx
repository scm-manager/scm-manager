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

import React, { useState } from "react";
import type { Meta, StoryObj } from "@storybook/react";
import DatePicker from "./DatePicker";

const meta: Meta<typeof DatePicker> = {
  title: "Forms/DatePicker",
  component: DatePicker,
  decorators: [(Story) => <div style={{ margin: "2rem", maxWidth: "20rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

const DefaultExample = () => {
  const [value, setValue] = useState("");
  return <DatePicker label="Date" value={value} onChange={setValue} />;
};

const WithBoundsExample = () => {
  const [value, setValue] = useState("2025-05-20");
  return <DatePicker label="Date" value={value} onChange={setValue} min="2025-05-10" max="2025-05-31" />;
};

export const Default: Story = {
  render: () => <DefaultExample />,
};

export const WithBounds: Story = {
  render: () => <WithBoundsExample />,
};
