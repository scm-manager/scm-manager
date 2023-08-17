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

import Popover from "./Popover";
import { ComponentMeta, ComponentStory } from "@storybook/react";
import React from "react";

export default {
  title: "Popover",
  component: Popover,
} as ComponentMeta<typeof Popover>;

const Template: ComponentStory<typeof Popover> = (args) => <Popover {...args} />;

export const WithShortBody = Template.bind({});
WithShortBody.args = {
  trigger: <button>a button</button>,
  title: <h1>A title longer than the body</h1>,
  children: <div>very short body</div>,
};

export const CiStatus = Template.bind({});
CiStatus.args = {
  trigger: (
    <button className={"is-borderless has-background-transparent"}>
      <i className={"fas fa-1x has-text-secondary fa-circle-notch"}></i>
    </button>
  ),
  title: <h1>Analyses: 0 error found</h1>,
  children: (
    <>
      <hr className={"my-2"} />
      <a className={"has-hover-background-blue is-flex is-flex-direction-row px-2 py-2 is-align-items-center"}>
        <i className={"fas fa-1x has-text-secondary fa-circle-notch mr-2"}></i>
        <span className="has-text-default">
          <strong>jenkins:</strong> scm-manager » scm-manager » develop
        </span>
      </a>
      <hr className={"my-2"} />
      <a className={"has-hover-background-blue is-flex is-flex-direction-row px-2 py-2 is-align-items-center"}>
        <i className={"fas fa-1x has-text-secondary fa-circle-notch mr-2"}></i>
        <span className={"has-text-default"}>
          <strong>Sonar:</strong> Sonar
        </span>
      </a>
    </>
  ),
};
