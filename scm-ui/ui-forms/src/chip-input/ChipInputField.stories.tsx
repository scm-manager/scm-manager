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

import { storiesOf } from "@storybook/react";
import React, { useRef, useState } from "react";
import ChipInputField from "./ChipInputField";
import Combobox from "../combobox/Combobox";
import { Option } from "@scm-manager/ui-types";
import ChipInput from "../headless-chip-input/ChipInput";

storiesOf("Chip Input Field", module)
  .add("Default", () => {
    const [value, setValue] = useState<Option<string>[]>([]);
    const ref = useRef<HTMLInputElement>(null);
    return (
      <>
        <ChipInputField
          value={value}
          onChange={setValue}
          label="Test Chips"
          placeholder="Type a new chip name and press enter to add"
          aria-label="My personal chip list"
          ref={ref}
        />
        <ChipInput.AddButton inputRef={ref}>Add</ChipInput.AddButton>
      </>
    );
  })
  .add("With Autocomplete", () => {
    const people = ["Durward Reynolds", "Kenton Towne", "Therese Wunsch", "Benedict Kessler", "Katelyn Rohan"];

    const [value, setValue] = useState<Option<string>[]>([]);

    return (
      <ChipInputField
        value={value}
        onChange={setValue}
        label="Persons"
        placeholder="Enter a new person"
        aria-label="Enter a new person"
      >
        <Combobox
          options={(query: string) =>
            Promise.resolve(
              people
                .map<Option<string>>((p) => ({ label: p, value: p }))
                .filter((t) => !value.some((val) => val.label === t.label) && t.label.startsWith(query))
                .concat({ label: query, value: query, displayValue: `Use '${query}'` })
            )
          }
        />
      </ChipInputField>
    );
  });
