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

import Input from "./Input";

const meta: Meta<typeof Input> = {
  title: "Forms/Input",
  component: Input,
  decorators: [(Story) => <div style={{ margin: "2rem", maxWidth: "40rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: () => <Input />,
};

export const WithVariant: Story = {
  render: () => <Input variant="danger" />,
};

export const WithCustomClass: Story = {
  render: () => <Input className="is-warning" />,
};

export const WithRef: Story = {
  name: "With Ref (Autofocus)",
  render: () => (
    <Input
      ref={(el) => {
        el?.focus();
      }}
    />
  ),
};
