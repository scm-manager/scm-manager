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
import { Button, Icon, Level } from "@scm-manager/ui-core";
import InputField from "./InputField";

type Props = {
  addEntry: (p: string) => void;
  disabled?: boolean;
  buttonLabel: string;
  fieldLabel: string;
  helpText?: string;
  validateEntry?: (p: string) => boolean;
  errorMessage: string;
};

const FullWidthInputField = styled(InputField)`
  width: 100%;
`;

/**
 *
 * @deprecated
 */
const AddEntryToTableField: FC<Props> = ({
  addEntry,
  disabled,
  buttonLabel,
  fieldLabel,
  helpText,
  validateEntry,
  errorMessage,
}) => {
  const [entryToAdd, setEntryToAdd] = useState("");

  const handleAddEntryChange = (entryName: string) => {
    setEntryToAdd(entryName);
  };

  const addButtonClicked = (event: MouseEvent | KeyboardEvent) => {
    event.preventDefault();
    appendEntry();
  };

  const appendEntry = () => {
    if (!disabled && entryToAdd !== "" && isValid()) {
      addEntry(entryToAdd);
      setEntryToAdd("");
    }
  };

  const isValid = () => {
    if (entryToAdd === "" || !validateEntry) {
      return true;
    } else {
      return validateEntry(entryToAdd);
    }
  };

  return (
    <Level
      className="is-align-items-stretch mb-4"
      right={
        <div className="field is-align-self-flex-end">
          <Button onClick={addButtonClicked} disabled={disabled || entryToAdd === "" || !isValid()}>
            <Icon>plus</Icon>
            <span>{buttonLabel}</span>
          </Button>
        </div>
      }
    >
      <FullWidthInputField
        className="mr-5"
        label={fieldLabel}
        errorMessage={errorMessage}
        onChange={handleAddEntryChange}
        validationError={!isValid()}
        value={entryToAdd}
        onReturnPressed={appendEntry}
        disabled={disabled}
        helpText={helpText}
      />
    </Level>
  );
};

export default AddEntryToTableField;
