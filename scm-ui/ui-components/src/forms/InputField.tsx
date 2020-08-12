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
import React, { ChangeEvent, KeyboardEvent } from "react";
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
  onChange: (value: string, name?: string) => void;
  onReturnPressed?: () => void;
  validationError?: boolean;
  errorMessage?: string;
  disabled?: boolean;
  helpText?: string;
  className?: string;
  testId?: string;
};

class InputField extends React.Component<Props> {
  static defaultProps = {
    type: "text",
    placeholder: ""
  };

  field: HTMLInputElement | null | undefined;

  componentDidMount() {
    if (this.props.autofocus && this.field) {
      this.field.focus();
    }
  }

  handleInput = (event: ChangeEvent<HTMLInputElement>) => {
    this.props.onChange(event.target.value, this.props.name);
  };

  handleKeyPress = (event: KeyboardEvent<HTMLInputElement>) => {
    const onReturnPressed = this.props.onReturnPressed;
    if (!onReturnPressed) {
      return;
    }
    if (event.key === "Enter") {
      event.preventDefault();
      onReturnPressed();
    }
  };

  render() {
    const {
      type,
      placeholder,
      value,
      validationError,
      errorMessage,
      disabled,
      label,
      helpText,
      className,
      testId
    } = this.props;
    const errorView = validationError ? "is-danger" : "";
    const helper = validationError ? <p className="help is-danger">{errorMessage}</p> : "";
    return (
      <div className={classNames("field", className)}>
        <LabelWithHelpIcon label={label} helpText={helpText} />
        <div className="control">
          <input
            ref={input => {
              this.field = input;
            }}
            className={classNames("input", errorView)}
            type={type}
            placeholder={placeholder}
            value={value}
            onChange={this.handleInput}
            onKeyPress={this.handleKeyPress}
            disabled={disabled}
            {...createAttributesForTesting(testId)}
          />
        </div>
        {helper}
      </div>
    );
  }
}

export default InputField;
