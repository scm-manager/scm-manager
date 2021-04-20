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
import React, { FC, useState, MouseEvent } from "react";
import styled from "styled-components";
import Level from "../layout/Level";
import AddButton from "../buttons/AddButton";
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

const StyledLevel = styled(Level)`
  align-items: stretch;
  margin-bottom: 1rem !important; // same margin as field
`;

const FullWidthInputField = styled(InputField)`
  width: 100%;
  margin-right: 1.5rem;
`;

const StyledField = styled.div.attrs(() => ({
  className: "field"
}))`
  align-self: flex-end;
`;

const AddEntryToTableField: FC<Props> = ({
  addEntry,
  disabled,
  buttonLabel,
  fieldLabel,
  helpText,
  validateEntry,
  errorMessage
}) => {
  const [entryToAdd, setEntryToAdd] = useState("");

  const handleAddEntryChange = (entryName: string) => {
    setEntryToAdd(entryName);
  };

  const addButtonClicked = (event: MouseEvent) => {
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
    <StyledLevel
      children={
        <FullWidthInputField
          label={fieldLabel}
          errorMessage={errorMessage}
          onChange={handleAddEntryChange}
          validationError={!isValid()}
          value={entryToAdd}
          onReturnPressed={appendEntry}
          disabled={disabled}
          helpText={helpText}
        />
      }
      right={
        <StyledField>
          <AddButton
            label={buttonLabel}
            action={addButtonClicked}
            disabled={disabled || entryToAdd === "" || !isValid()}
          />
        </StyledField>
      }
    />
  );
};

export default AddEntryToTableField;
