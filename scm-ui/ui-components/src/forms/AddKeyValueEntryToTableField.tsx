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

/**
 * @deprecated
 */
const AddKeyValueEntryToTableField: FC<Props> = ({
  keyFieldLabel,
  valueFieldLabel,
  disabled,
  buttonLabel,
  keyHelpText,
  valueHelpText,
  validateEntry,
  errorMessage,
  addEntry,
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
