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

// import StoryRouter from "storybook-react-router";
// import { ComponentMeta, StoryFn } from "@storybook/react";
// import React, { ComponentProps } from "react";
// import { Button } from "../../buttons";
// import Dialog from "./Dialog";
//
// export default {
//   title: "Dialog",
//   component: Dialog,
//   decorators: [
//     StoryRouter(),
//     (Story) => (
//       <div style={{ height: "100vh" }}>
//         <Story />
//       </div>
//     ),
//   ],
// } as ComponentMeta<typeof Dialog>;
//
// const Template: StoryFn<ComponentProps<typeof Dialog>> = (args) => <Dialog {...args} />;
//
// export const Default = Template.bind({});
// // More on args: https://storybook.js.org/docs/react/writing-stories/args
// Default.args = {
//   title: "My Dialog",
//   trigger: <Button>Open Dialog</Button>,
//   description: "Do you really want to do this ?",
//   children: (
//     <table>
//       <tr>
//         <th>Yes</th>
//         <th>No</th>
//       </tr>
//       <tr>
//         <td>42</td>
//         <td>12</td>
//       </tr>
//     </table>
//   ),
//   footer: [
//     <Button>Do it</Button>,
//     <Button variant="primary" autoFocus>
//       Cancel
//     </Button>,
//   ],
// } as ComponentProps<typeof Dialog>;

import React from "react";
import type { Meta, StoryObj } from "@storybook/react";
import { Button } from "../../buttons";
import Dialog from "./Dialog";

const meta: Meta<typeof Dialog> = {
  title: "Components/Dialog",
  component: Dialog,
  decorators: [
    (Story) => (
      <div style={{ height: "100vh", padding: "2rem" }}>
        <Story />
      </div>
    ),
  ],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof Dialog>;

export const Default: Story = {
  args: {
    title: "My Dialog",
    trigger: <Button>Open Dialog</Button>,
    description: "Do you really want to do this?",
    children: (
      <table>
        <thead>
          <tr>
            <th>Yes</th>
            <th>No</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>42</td>
            <td>12</td>
          </tr>
        </tbody>
      </table>
    ),
    // footer: (
    //   <>
    //     <Button>Do it</Button>
    //     <Button variant="primary" autoFocus>
    //       Cancel
    //     </Button>
    //   </>
    // ),
  },
};

export const Fullscreen: Story = {
  args: {
    ...Default.args,
    title: "My Fullscreen Dialog",
    size: "fullscreen",
  },
};
