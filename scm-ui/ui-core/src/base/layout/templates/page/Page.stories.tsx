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
import type { Meta, StoryObj } from "@storybook/react";
import Page from "./Page";
import { Button, ButtonVariants } from "../../../buttons";

const meta: Meta<typeof Page> = {
  title: "Templates/Page",
  component: Page,
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

const Content = () => <p>The content of the page.</p>;

export const Default: Story = {
  render: () => (
    <Page title="Repositories" subtitle="Overview of all repositories">
      <Content />
    </Page>
  ),
};

export const WithActions: Story = {
  render: () => (
    <Page
      title="Repositories"
      subtitle="Overview of all repositories"
      actions={<Button variant={ButtonVariants.PRIMARY}>Add Repository</Button>}
    >
      <Content />
    </Page>
  ),
};

export const WithAfterTitle: Story = {
  render: () => (
    <Page title="Add Repository" subtitle="Create a new repository" afterTitle={<span className="tag">Beta</span>}>
      <Content />
    </Page>
  ),
};

export const Loading: Story = {
  render: () => (
    <Page title="Repositories" subtitle="Overview of all repositories" loading>
      <Content />
    </Page>
  ),
};

export const WithError: Story = {
  render: () => (
    <Page
      title="Repositories"
      subtitle="Overview of all repositories"
      actions={<Button variant={ButtonVariants.PRIMARY}>Add Repository</Button>}
      error={new Error("The repositories could not be loaded")}
    >
      <Content />
    </Page>
  ),
};

export const WithErrorAndContent: Story = {
  render: () => (
    <Page
      title="Create User"
      subtitle="Create a new user"
      error={new Error("The user could not be created")}
      showContentOnError
    >
      <Content />
    </Page>
  ),
};
