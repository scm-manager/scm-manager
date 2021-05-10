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
import React, { ChangeEvent, KeyboardEvent, FocusEvent, FC, useRef, useEffect } from "react";
import classNames from "classnames";
import LabelWithHelpIcon from "./LabelWithHelpIcon";
import { createAttributesForTesting } from "../devBuild";

type Props = {
  label?: string;
  name?: string;
  placeholder?: string;
  value?: string;
  type?: string;
  autofocus?: boolean;
  onChange?: (value: string, name?: string) => void;
  onReturnPressed?: () => void;
  validationError?: boolean;
  errorMessage?: string;
  informationMessage?: string;
  disabled?: boolean;
  helpText?: string;
  className?: string;
  testId?: string;
  onBlur?: (value: string, name?: string) => void;
};

const InputField: FC<Props> = ({
  name,
  onChange,
  onBlur,
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
  testId
}) => {
  const field = useRef<HTMLInputElement>(null);
  useEffect(() => {
    if (field.current) {
      field.current.focus();
    }
  }, [field]);

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (onChange) {
      onChange(event.target.value, name);
    }
  };

  const handleBlur = (event: FocusEvent<HTMLInputElement>) => {
    if (onBlur) {
      onBlur(event.target.value, name);
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
    <div className={classNames("field", className)}>
      <LabelWithHelpIcon label={label} helpText={helpText} />
      <div className="control">
        <input
          ref={field}
          className={classNames("input", errorView)}
          type={type}
          placeholder={placeholder}
          value={value}
          disabled={disabled}
          onChange={handleChange}
          onKeyPress={handleKeyPress}
          onBlur={handleBlur}
          {...createAttributesForTesting(testId)}
        />
      </div>
      {helper}
    </div>
  );
};

export default InputField;
