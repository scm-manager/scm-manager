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

import TextareaField from "./TextareaField";

const meta: Meta<typeof TextareaField> = {
  title: "Forms/TextareaField",
  component: TextareaField,
  decorators: [(Story) => <div style={{ margin: "2rem", maxWidth: "40rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: () => <TextareaField label="MyTextarea" />,
};

export const WithDescription: Story = {
  render: () => <TextareaField label="MyTextarea" descriptionText="This text is a description for this textarea" />,
};

export const WithError: Story = {
  render: () => <TextareaField label="MyTextarea" error="This field is super required" />,
};

export const Required: Story = {
  render: () => <TextareaField label="MyTextarea" required />,
};

export const WithWidth: Story = {
  render: () => <TextareaField label="MyTextarea" className="column is-half" />,
};

export const WithDefaultValue: Story = {
  render: () => <TextareaField label="MyTextarea" defaultValue="value" />,
};
