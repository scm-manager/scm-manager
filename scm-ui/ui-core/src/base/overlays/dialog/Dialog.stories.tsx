/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
