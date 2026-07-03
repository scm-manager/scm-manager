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

import InputField from "./InputField";

const meta: Meta<typeof InputField> = {
  title: "Forms/InputField",
  component: InputField,
  decorators: [(Story) => <div style={{ margin: "2rem", maxWidth: "40rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: () => <InputField label="MyInput" />,
};

export const WithDescription: Story = {
  render: () => <InputField label="MyInput" descriptionText="This text is a description for this input" />,
};

export const WithDescriptionExtended: Story = {
  render: () => (
    <>
      <InputField
        label="MyInput 1"
        descriptionText="This text is a description for this input"
        extendedText="This is the extended description for this input"
      />
      <InputField
        label="MyInput 2"
        descriptionText="This text is a description for this input"
        extendedText="This is the long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long extended description for this input"
      />
    </>
  ),
};

export const WithError: Story = {
  render: () => <InputField label="MyInput" error="This field is super required" />,
};

export const WithWidth: Story = {
  render: () => <InputField label="MyInput" className="column is-half" />,
};

export const WithIcon: Story = {
  render: () => <InputField label="MyInput" icon="fas fa-filter" />,
};
