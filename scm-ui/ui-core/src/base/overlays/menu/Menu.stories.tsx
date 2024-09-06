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
import Menu, { MenuButton, MenuDialog, MenuExternalLink, MenuLink } from "./Menu";
import { Button, Icon } from "../../buttons";
import { CloseButton } from "../dialog/Dialog";

export default {
  title: "Menu",
  component: Menu,
  decorators: [StoryRouter()],
} as ComponentMeta<typeof Menu>;

const Template: StoryFn<ComponentProps<typeof Menu>> = (args) => <Menu {...args} />;

export const Default = Template.bind({});
// More on args: https://storybook.js.org/docs/react/writing-stories/args
Default.args = {
  children: [
    // eslint-disable-next-line no-console
    <MenuButton onSelect={() => console.log("A button has been clicked")}>
      <Icon />A button
    </MenuButton>,
    <MenuLink to="/repos">
      <Icon />A link
    </MenuLink>,
    <MenuExternalLink href="https://scm-manager.org">
      <Icon>link</Icon>An external link
    </MenuExternalLink>,
    <MenuButton disabled>
      <Icon>trash</Icon>A disabled button
    </MenuButton>,
    <MenuDialog
      title="My Dialog"
      description="Do you really want to do this ?"
      footer={[<Button autoFocus>Do it</Button>, <CloseButton variant="primary">Cancel</CloseButton>]}
      dialogContent={
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
      }
    >
      <Icon />
      Open Dialog
    </MenuDialog>,
  ],
} as ComponentProps<typeof Menu>;
