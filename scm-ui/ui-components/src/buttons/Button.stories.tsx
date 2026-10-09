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

import React, { useEffect, useState } from "react";
import styled from "styled-components";
import type { Meta, StoryObj } from "@storybook/react";
import Button from "./Button";

const colors = ["primary", "link", "info", "success", "warning", "danger", "white", "light", "dark", "black", "text"];
const Spacing = styled.div`
  padding: 1em;
  display: inline-block;
`;

const RefExample = () => {
  const [ref, setRef] = useState<HTMLButtonElement | HTMLAnchorElement | null>(null);
  useEffect(() => {
    ref?.focus();
  }, [ref]);
  return <Button ref={setRef}>Focus me!</Button>;
};

const meta: Meta<typeof Button> = {
  title: "Buttons/Button",
  component: Button,
  decorators: [(Story) => <Spacing>{Story()}</Spacing>],
  tags: ["autodocs"],
};

export default meta;

// @ts-ignore This type is too complex, no clue what to do about it
type Story = StoryObj<typeof meta>;

export const Loading: Story = {
  args: {
    color: "primary",
    loading: true,
    children: "Loading Button",
  },
};

export const Colors: Story = {
  render: () => (
    <div>
      {colors.map((color) => (
        <Spacing key={color}>
          <Button color={color} label={color} />
        </Spacing>
      ))}
    </div>
  ),
};

export const Disabled: Story = {
  render: () => (
    <div>
      {colors.map((color) => (
        <Spacing key={color}>
          <Button color={color} label={color} disabled={true} />
        </Spacing>
      ))}
    </div>
  ),
};

export const UsingRefs: Story = {
  name: "Using Refs",
  render: () => <RefExample />,
};
