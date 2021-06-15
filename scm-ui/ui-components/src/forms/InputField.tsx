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
import React, { ChangeEvent, FC, FocusEvent, KeyboardEvent } from "react";
import classNames from "classnames";
import LabelWithHelpIcon from "./LabelWithHelpIcon";
import { createAttributesForTesting } from "../devBuild";
import useAutofocus from "./useAutofocus";
import { createFormFieldWrapper, FieldProps, FieldType, isLegacy, isUsingRef } from "./FormFieldTypes";

type BaseProps = {
  label?: string;
  name?: string;
  placeholder?: string;
  value?: string;
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
  defaultValue?: string;
  readOnly?: boolean;
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
  return (
    <fieldset className={classNames("field", className)} disabled={readOnly}>
      <LabelWithHelpIcon label={label} helpText={helpText} />
      <div className="control">
        <input
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
          {...createAttributesForTesting(testId)}
        />
      </div>
      {helper}
    </fieldset>
  );
};

const InputField: FieldType<BaseProps, HTMLInputElement, string> = createFormFieldWrapper(InnerInputField);

export default InputField;
