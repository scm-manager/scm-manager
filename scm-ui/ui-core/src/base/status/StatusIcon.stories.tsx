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

import React, { ComponentProps } from "react";

import StoryRouter from "storybook-react-router";
import { StoryFn } from "@storybook/react";
import { StatusIcon } from "./index";
import { StatusIconSizeVariantList, StatusVariantList, StatusVariants } from "./StatusIcon";

// More on default export: https://storybook.js.org/docs/react/writing-stories/introduction#default-export
export default {
  title: "Status",
  component: null,
  subcomponents: {
    Button: StatusIcon,
  },
  argTypes: {
    variant: {
      options: StatusVariantList,
      control: { type: "select" },
    },
    iconSize: {
      options: StatusIconSizeVariantList,
      control: { type: "select" },
    },
  },
  decorators: [StoryRouter()],
  parameters: {
    storyshots: { disable: true },
  },
};

// More on component templates: https://storybook.js.org/docs/react/writing-stories/introduction#using-args
const StatusIconTemplate: StoryFn<ComponentProps<typeof StatusIcon>> = (args) => <StatusIcon {...args} />;

export const Icon = StatusIconTemplate.bind({});
Icon.args = {
  variant: StatusVariants.SUCCESS,
  invert: false,
  iconSize: "lg",
};

export const IconWithTitle = StatusIconTemplate.bind({});
IconWithTitle.args = {
  variant: StatusVariants.SUCCESS,
  invert: false,
  iconSize: "lg",
  children: "Lorem Ipsum",
};
