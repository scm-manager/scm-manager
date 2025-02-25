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

import React, { ChangeEvent, FC, FocusEvent, KeyboardEvent } from "react";
import classNames from "classnames";
import LabelWithHelpIcon from "./LabelWithHelpIcon";
import { createAttributesForTesting } from "../devBuild";
import useAutofocus from "./useAutofocus";
import { createFormFieldWrapper, FieldProps, FieldType, isLegacy, isUsingRef } from "./FormFieldTypes";
import { createA11yId } from "../createA11yId";
import FieldMessage from "@scm-manager/ui-core/src/base/forms/base/field-message/FieldMessage";

type BaseProps = {
  label?: string;
  name?: string;
  placeholder?: string;
  value?: string | number;
  type?: string;
  autofocus?: boolean;
  onReturnPressed?: () => void;
  validationError?: boolean;
  errorMessage?: string | string[];
  informationMessage?: string;
  disabled?: boolean;
  helpText?: string;
  className?: string;
  testId?: string;
  defaultValue?: string | number;
  readOnly?: boolean;
  required?: boolean;
  warning?: string;
};

export const InnerInputField: FC<FieldProps<BaseProps, HTMLInputElement, string>> = ({
  name,
  onReturnPressed,
  type,
  placeholder,
  value,
  validationError,
  errorMessage,
  informationMessage,
  disabled,
  label,
  helpText,
  className,
  testId,
  autofocus,
  defaultValue,
  readOnly,
  required,
  warning,
  ...props
}) => {
  const field = useAutofocus<HTMLInputElement>(autofocus, props.innerRef);

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (props.onChange) {
      if (isUsingRef<BaseProps, HTMLInputElement, string>(props)) {
        props.onChange(event);
      } else if (isLegacy(props)) {
        props.onChange(event.target.value, name);
      }
    }
  };

  const handleBlur = (event: FocusEvent<HTMLInputElement>) => {
    if (props.onBlur) {
      if (isUsingRef<BaseProps, HTMLInputElement, string>(props)) {
        props.onBlur(event);
      } else if (isLegacy(props)) {
        props.onBlur(event.target.value, name);
      }
    }
  };

  const handleKeyPress = (event: KeyboardEvent<HTMLInputElement>) => {
    if (onReturnPressed && event.key === "Enter") {
      event.preventDefault();
      onReturnPressed();
    }
  };

  const errorView = validationError ? "is-danger" : "";
  let helper;
  if (validationError) {
    helper = <p className="help is-danger">{errorMessage}</p>;
  } else if (informationMessage) {
    helper = <p className="help is-info">{informationMessage}</p>;
  }

  const id = createA11yId("input");
  const helpId = createA11yId("input");
  return (
    <fieldset className={classNames("field", className)} disabled={readOnly}>
      <LabelWithHelpIcon label={label} helpText={helpText} id={id} helpId={helpId} required={required} />
      <div className="control">
        <input
          aria-labelledby={id}
          aria-describedby={helpId}
          ref={field}
          name={name}
          className={classNames("input", errorView)}
          type={type}
          placeholder={placeholder}
          value={value}
          disabled={disabled}
          onChange={handleChange}
          onKeyPress={handleKeyPress}
          onBlur={handleBlur}
          defaultValue={defaultValue}
          aria-required={required}
          {...createAttributesForTesting(testId)}
        />
      </div>
      {warning ? <FieldMessage variant="warning">{warning}</FieldMessage> : null}
      {helper}
    </fieldset>
  );
};

const InputField: FieldType<BaseProps, HTMLInputElement, string> = createFormFieldWrapper(InnerInputField);

export default InputField;
