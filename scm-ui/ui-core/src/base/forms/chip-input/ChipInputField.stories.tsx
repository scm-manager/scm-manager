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

// import { storiesOf } from "@storybook/react";
// import React, { useRef, useState } from "react";
// import ChipInputField from "./ChipInputField";
// import Combobox from "../combobox/Combobox";
// import { Option } from "@scm-manager/ui-types";
// import ChipInput from "../headless-chip-input/ChipInput";
//
// storiesOf("Chip Input Field", module)
//   .add("Default", () => {
//     const [value, setValue] = useState<Option<string>[]>([]);
//     const ref = useRef<HTMLInputElement>(null);
//     return (
//       <>
//         <ChipInputField
//           value={value}
//           onChange={setValue}
//           label="Test Chips"
//           placeholder="Type a new chip name and press enter to add"
//           aria-label="My personal chip list"
//           ref={ref}
//         />
//         <ChipInput.AddButton inputRef={ref}>Add</ChipInput.AddButton>
//       </>
//     );
//   })
//   .add("With Autocomplete", () => {
//     const people = ["Durward Reynolds", "Kenton Towne", "Therese Wunsch", "Benedict Kessler", "Katelyn Rohan"];
//
//     const [value, setValue] = useState<Option<string>[]>([]);
//
//     return (
//       <ChipInputField
//         value={value}
//         onChange={setValue}
//         label="Persons"
//         placeholder="Enter a new person"
//         aria-label="Enter a new person"
//       >
//         <Combobox
//           options={(query: string) =>
//             Promise.resolve(
//               people
//                 .map<Option<string>>((p) => ({ label: p, value: p }))
//                 .filter((t) => !value.some((val) => val.label === t.label) && t.label.startsWith(query))
//                 .concat({ label: query, value: query, displayValue: `Use '${query}'` })
//             )
//           }
//         />
//       </ChipInputField>
//     );
//   });

import React, { useRef, useState } from "react";
import type { Meta, StoryObj } from "@storybook/react";
import { Option } from "@scm-manager/ui-types";

import ChipInputField from "./ChipInputField";
import ChipInput from "../headless-chip-input/ChipInput";
import Combobox from "../combobox/Combobox";

const DefaultExample = () => {
  const [value, setValue] = useState<Option<string>[]>([]);
  const ref = useRef<HTMLInputElement>(null);

  return (
    <>
      {/* @ts-ignore */}
      <ChipInputField
        value={value}
        onChange={setValue}
        label="Test Chips"
        placeholder="Type a new chip name and press enter to add"
        aria-label="My personal chip list"
        ref={ref}
      />
      <ChipInput.AddButton inputRef={ref} className="button is-link is-outlined ml-1 mt-2">
        Add
      </ChipInput.AddButton>
    </>
  );
};

const WithAutocompleteExample = () => {
  const people = ["Durward Reynolds", "Kenton Towne", "Therese Wunsch", "Benedict Kessler", "Katelyn Rohan"];
  const [value, setValue] = useState<Option<string>[]>([]);

  return (
    <>
      {/* @ts-ignore */}
      <ChipInputField
        value={value}
        onChange={setValue}
        label="Persons"
        placeholder="Enter a new person"
        aria-label="Enter a new person"
      >
        {/* @ts-ignore */}
        <Combobox
          options={(query: string) =>
            Promise.resolve(
              people
                .map<Option<string>>((p) => ({ label: p, value: p }))
                .filter((t) => !value.some((val) => val.label === t.label) && t.label.startsWith(query))
                .concat({ label: query, value: query, displayValue: `Use '${query}'` }),
            )
          }
        />
      </ChipInputField>
    </>
  );
};

const meta: Meta<typeof ChipInputField> = {
  title: "Forms/ChipInputField",

  // @ts-ignore
  component: ChipInputField,
  decorators: [(Story) => <div style={{ margin: "2rem", maxWidth: "40rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: () => <DefaultExample />,
};

export const WithAutocomplete: Story = {
  render: () => <WithAutocompleteExample />,
};
