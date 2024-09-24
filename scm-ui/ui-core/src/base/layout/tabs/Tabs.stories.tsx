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
import StoryRouter from "storybook-react-router";
import { ComponentMeta, ComponentStory } from "@storybook/react";
import Tabs from "./Tabs";
import TabsList from "./TabsList";
import TabTrigger from "./TabTrigger";
import TabsContent from "./TabsContent";

export default {
  title: "Tab",
  component: Tabs,
  decorators: [StoryRouter()],
} as ComponentMeta<typeof Tabs>;

export const Default: ComponentStory<typeof Tabs> = () => (
  <Tabs defaultValue="tab2">
    <TabsList>
      <TabTrigger value="tab1">Account</TabTrigger>
      <TabTrigger value="tab2">Password</TabTrigger>
    </TabsList>
    <TabsContent value="tab1">Account Settings</TabsContent>
    <TabsContent value="tab2">Password Settings</TabsContent>
  </Tabs>
);
