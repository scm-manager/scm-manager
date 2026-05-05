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

// import React from "react";
// import { MemoryRouter } from "react-router-dom";
// import { storiesOf } from "@storybook/react";
// import AddKeyValueEntryToTableField from "./AddKeyValueEntryToTableField";
//
// storiesOf("Forms/AddKeyValueEntryToTableField", module)
//   .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
//   .add("Default", () => (
//     <div className="m-6">
//       <AddKeyValueEntryToTableField
//         keyFieldLabel="Key"
//         valueFieldLabel="Value"
//         buttonLabel="Add to Table"
//         addEntry={() => null}
//       />
//     </div>
//   ))
//   .add("Disabled", () => (
//     <div className="m-6">
//       <AddKeyValueEntryToTableField
//         keyFieldLabel="Key"
//         valueFieldLabel="Value"
//         buttonLabel="Add to Table"
//         addEntry={() => null}
//         disabled={true}
//       />
//     </div>
//   ));

import React from "react";
import type { Meta, StoryObj } from "@storybook/react";

import AddKeyValueEntryToTableField from "./AddKeyValueEntryToTableField";

const meta: Meta<typeof AddKeyValueEntryToTableField> = {
  title: "Forms/AddKeyValueEntryToTableField",
  component: AddKeyValueEntryToTableField,
  decorators: [(Story) => Story(), (Story) => <div className="m-6">{Story()}</div>],
  argTypes: {
    addEntry: { action: "entryAdded" },
  },
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    keyFieldLabel: "Key",
    valueFieldLabel: "Value",
    buttonLabel: "Add to Table",
    disabled: false,
  },
};

export const Disabled: Story = {
  args: {
    ...Default.args,
    disabled: true,
  },
};
