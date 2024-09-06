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
