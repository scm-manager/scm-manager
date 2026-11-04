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
import type { Meta, StoryObj } from "@storybook/react";

import FileInputField from "./FileInputField";

const meta: Meta<typeof FileInputField> = {
  title: "Forms/FileInputField",
  component: FileInputField,
  decorators: [(Story) => <div style={{ margin: "2rem", maxWidth: "40rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: () => <FileInputField label="MyFile" />,
};

export const WithDescription: Story = {
  render: () => <FileInputField label="MyFile" descriptionText="This text is a description for this file input" />,
};

export const WithPlaceholder: Story = {
  render: () => <FileInputField label="MyFile" filenamePlaceholder="No file chosen" />,
};

export const Required: Story = {
  render: () => <FileInputField label="MyFile" required />,
};

export const WithError: Story = {
  render: () => <FileInputField label="MyFile" error="This field is super required" />,
};
