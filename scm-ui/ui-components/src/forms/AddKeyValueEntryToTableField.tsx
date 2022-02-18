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
import React, { FC, useState } from "react";
import styled from "styled-components";
import InputField from "./InputField";
import { AddButton } from "../buttons";
import { devices } from "../devices";

type Props = {
  addEntry: (key: string, value: string) => void;
  disabled?: boolean;
  buttonLabel: string;
  keyFieldLabel: string;
  valueFieldLabel: string;
  errorMessage?: string;
  keyHelpText?: string;
  valueHelpText?: string;
  validateEntry?: (p: string) => boolean;
};

const FullWidthInputField = styled(InputField)`
  width: 100%;
`;

const MobileWrappedDiv = styled.div`
  @media screen and (min-width: ${devices.tablet.width}px) {
    & > ${FullWidthInputField} {
      margin-right: 1.5rem;
    }
  }
  @media screen and (max-width: ${devices.mobile.width}px) {
    flex-wrap: wrap;
  }
`;

const AddKeyValueEntryToTableField: FC<Props> = ({
  keyFieldLabel,
  valueFieldLabel,
  disabled,
  buttonLabel,
  keyHelpText,
  valueHelpText,
  validateEntry,
  errorMessage,
  addEntry
}) => {
  const [key, setKey] = useState("");
  const [value, setValue] = useState("");

  const isValid = (input: string) => {
    if (!input || !validateEntry) {
      return true;
    } else {
      return validateEntry(input);
    }
  };

  const add = () => {
    addEntry(key, value);
    setKey("");
    setValue("");
  };

  return (
    <MobileWrappedDiv className="is-flex is-align-items-flex-end">
      <FullWidthInputField
        label={keyFieldLabel}
        errorMessage={errorMessage}
        onChange={setKey}
        validationError={!isValid(key)}
        value={key}
        onReturnPressed={add}
        disabled={disabled}
        helpText={keyHelpText}
      />
      <FullWidthInputField
        label={valueFieldLabel}
        errorMessage={errorMessage}
        onChange={setValue}
        validationError={!isValid(value)}
        value={value}
        onReturnPressed={add}
        disabled={disabled}
        helpText={valueHelpText}
      />
      <AddButton
        className="ml-auto mb-3"
        label={buttonLabel}
        action={add}
        disabled={disabled || !key || !value || !isValid(key) || !isValid(value)}
      />
    </MobileWrappedDiv>
  );
};

export default AddKeyValueEntryToTableField;
