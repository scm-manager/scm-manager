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
