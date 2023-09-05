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
import React, { FC, useState, MouseEvent, KeyboardEvent } from "react";
import styled from "styled-components";
import { SelectValue } from "@scm-manager/ui-types";
import Level from "../layout/Level";
import AddButton from "../buttons/AddButton";
import Autocomplete from "../Autocomplete";

type Props = {
  addEntry: (p: SelectValue) => void;
  disabled?: boolean;
  buttonLabel: string;
  fieldLabel?: string;
  helpText?: string;
  loadSuggestions: (p: string) => Promise<SelectValue[]>;
  placeholder?: string;
  loadingMessage?: string;
  noOptionsMessage?: string;
};

const FullWidthAutocomplete = styled(Autocomplete)`
  width: 100%;
`;

/**
 * @deprecated
 */
const AutocompleteAddEntryToTableField: FC<Props> = ({
  addEntry,
  disabled,
  buttonLabel,
  fieldLabel,
  helpText,
  loadSuggestions,
  placeholder,
  loadingMessage,
  noOptionsMessage,
}) => {
  const [selectedValue, setSelectedValue] = useState<SelectValue | undefined>(undefined);

  const handleAddEntryChange = (selection: SelectValue) => {
    setSelectedValue(selection);
  };

  const addButtonClicked = (event: MouseEvent | KeyboardEvent) => {
    event.preventDefault();
    appendEntry();
  };

  const appendEntry = () => {
    if (disabled || !selectedValue) {
      return;
    }
    addEntry(selectedValue);
    setSelectedValue(undefined);
  };

  return (
    <Level
      children={
        <FullWidthAutocomplete
          className="mr-5"
          label={fieldLabel}
          loadSuggestions={loadSuggestions}
          valueSelected={handleAddEntryChange}
          helpText={helpText}
          value={selectedValue}
          placeholder={placeholder}
          loadingMessage={loadingMessage}
          noOptionsMessage={noOptionsMessage}
          creatable={true}
        />
      }
      right={
        <div className="field">
          <AddButton label={buttonLabel} action={addButtonClicked} disabled={disabled} />
        </div>
      }
    />
  );
};

export default AutocompleteAddEntryToTableField;
