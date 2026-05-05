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
// import { storiesOf } from "@storybook/react";
// import DropDown from "./DropDown";
//
// storiesOf("Forms/DropDown", module)
//   .add("Default", () => (
//     <DropDown
//       options={["en", "de", "es"]}
//       preselectedOption="de"
//       optionSelected={() => {
//         // nothing to do
//       }}
//     />
//   ))
//   .add("With Translation", () => (
//     <DropDown
//       optionValues={["hg2g", "dirk", "liff"]}
//       options={[
//         "The Hitchhiker's Guide to the Galaxy",
//         "Dirk Gently’s Holistic Detective Agency",
//         "The Meaning Of Liff",
//       ]}
//       preselectedOption="dirk"
//       optionSelected={(selection) => {
//         // nothing to do
//       }}
//     />
//   ))
//   .add("Add preselect if missing in options", () => (
//     <DropDown
//       optionValues={["alpha", "beta", "gamma"]}
//       options={["A", "B", "C"]}
//       preselectedOption="D"
//       optionSelected={(selection) => {
//         // nothing to do
//       }}
//     />
//   ));

import React from "react";
import type { Meta, StoryObj } from "@storybook/react";

import DropDown from "./DropDown";

const meta: Meta<typeof DropDown> = {
  title: "Forms/DropDown",
  component: DropDown,
  parameters: {
    layout: "centered",
  },
  argTypes: {
    optionSelected: { action: "optionSelected" },
  },
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    options: ["en", "de", "es"],
    preselectedOption: "de",
  },
};

export const WithDisplayValues: Story = {
  name: "With Custom Display Values",
  args: {
    optionValues: ["hg2g", "dirk", "liff"],
    options: ["The Hitchhiker's Guide to the Galaxy", "Dirk Gently’s Holistic Detective Agency", "The Meaning Of Liff"],
    preselectedOption: "dirk",
  },
};

export const PreselectedOptionNotInList: Story = {
  name: "Preselected Value Not In Options",
  args: {
    optionValues: ["alpha", "beta", "gamma"],
    options: ["A", "B", "C"],
    preselectedOption: "D",
  },
};
