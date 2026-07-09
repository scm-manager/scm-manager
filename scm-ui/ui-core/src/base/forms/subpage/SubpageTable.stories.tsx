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

import SubpageTable from "./SubpageTable";
import { Icon, IconButton } from "../../buttons";

const meta: Meta<typeof SubpageTable> = {
  title: "Forms/SubpageTable",
  component: SubpageTable,
  decorators: [(Story) => <div style={{ margin: "2rem", maxWidth: "40rem" }}>{Story()}</div>],
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "A read-only table for the overview of a `SubpageForm` that stays usable on mobile: below 768px the " +
          "`thead` is hidden and each cell shows its column title from its `data-label` attribute. Cell content " +
          "is vertically centered. **Resize the preview below 768px** (or use the Viewport toolbar) to see the " +
          "responsive layout.",
      },
    },
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

const recipients = [
  { email: "beate-paulina.berlin@standardemail.de", type: "Committer" },
  { email: "paul.bernd@email.de", type: "Author" },
];

export const Default: Story = {
  render: () => (
    <SubpageTable>
      <thead>
        <tr>
          <th>Email address</th>
          <th>Send as</th>
        </tr>
      </thead>
      <tbody>
        {recipients.map((recipient) => (
          <tr key={recipient.email}>
            <td data-label="Email address">{recipient.email}</td>
            <td data-label="Send as">{recipient.type}</td>
          </tr>
        ))}
      </tbody>
    </SubpageTable>
  ),
};

export const WithRowActions: Story = {
  parameters: {
    docs: {
      description: {
        story:
          "Row actions use a bordered `IconButton`. The table gives every row the same height and centers cell " +
          "content vertically, so the button lines up with the row text. Inside a `SubpageForm` you would use " +
          "`OpenIconButton`, which renders the same button.",
      },
    },
  },
  render: () => (
    <SubpageTable>
      <thead>
        <tr>
          <th>Email address</th>
          <th>Send as</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {recipients.map((recipient) => (
          <tr key={recipient.email}>
            <td data-label="Email address">{recipient.email}</td>
            <td data-label="Send as">{recipient.type}</td>
            <td data-label="Actions">
              <IconButton aria-label={`Edit ${recipient.email}`}>
                <Icon>edit</Icon>
              </IconButton>
            </td>
          </tr>
        ))}
      </tbody>
    </SubpageTable>
  ),
};

export const MixedRowActions: Story = {
  parameters: {
    docs: {
      description: {
        story:
          "Rows keep a uniform height whether or not they carry an action: the fixed row height reserves room " +
          "for the button, so a row with a button and a plain text row are the same height.",
      },
    },
  },
  render: () => (
    <SubpageTable>
      <thead>
        <tr>
          <th>Email address</th>
          <th>Send as</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td data-label="Email address">beate-paulina.berlin@standardemail.de</td>
          <td data-label="Send as">Committer</td>
          <td data-label="Actions">
            <IconButton aria-label="Edit beate-paulina.berlin@standardemail.de">
              <Icon>edit</Icon>
            </IconButton>
          </td>
        </tr>
        <tr>
          <td data-label="Email address">paul.bernd@email.de</td>
          <td data-label="Send as">Author</td>
          <td data-label="Actions" />
        </tr>
      </tbody>
    </SubpageTable>
  ),
};

export const Empty: Story = {
  parameters: {
    docs: {
      description: {
        story:
          "When the table has no data rows, render a single `SubpageTable.EmptyRow` inside the `<tbody>`. It spans " +
          "all columns and shows an informational notification. Pass a context-specific message as its children, or " +
          "omit it to fall back to the generic `commons:form.table.empty.label` text (see the *Fallback message* story).",
      },
    },
  },
  render: () => (
    <SubpageTable>
      <thead>
        <tr>
          <th>Email address</th>
          <th>Send as</th>
        </tr>
      </thead>
      <tbody>
        <SubpageTable.EmptyRow colSpan={2}>No recipients have been added yet.</SubpageTable.EmptyRow>
      </tbody>
    </SubpageTable>
  ),
};
