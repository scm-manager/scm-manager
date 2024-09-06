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

import StoryRouter from "storybook-react-router";
import { ComponentMeta, StoryFn } from "@storybook/react";
import React, { ComponentProps } from "react";
import { Button } from "../../buttons";
import Dialog from "./Dialog";

export default {
  title: "Dialog",
  component: Dialog,
  decorators: [StoryRouter()],
  render: (story) => <div style={{ height: "100vh" }}>{story}</div>,
} as ComponentMeta<typeof Dialog>;

const Template: StoryFn<ComponentProps<typeof Dialog>> = (args) => <Dialog {...args} />;

export const Default = Template.bind({});
// More on args: https://storybook.js.org/docs/react/writing-stories/args
Default.args = {
  title: "My Dialog",
  trigger: <Button>Open Dialog</Button>,
  description: "Do you really want to do this ?",
  children: (
    <table>
      <tr>
        <th>Yes</th>
        <th>No</th>
      </tr>
      <tr>
        <td>42</td>
        <td>12</td>
      </tr>
    </table>
  ),
  footer: [
    <Button>Do it</Button>,
    <Button variant="primary" autoFocus>
      Cancel
    </Button>,
  ],
} as ComponentProps<typeof Dialog>;
