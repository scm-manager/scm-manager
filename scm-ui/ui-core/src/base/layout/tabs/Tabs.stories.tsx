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
