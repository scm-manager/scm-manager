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
import React from "react";
import classNames from "classnames";
import { Async, AsyncCreatable } from "react-select";
import { SelectValue } from "@scm-manager/ui-types";
import LabelWithHelpIcon from "./forms/LabelWithHelpIcon";
import { ActionMeta, ValueType } from "react-select/lib/types";

type Props = {
  loadSuggestions: (p: string) => Promise<SelectValue[]>;
  valueSelected: (p: SelectValue) => void;
  label?: string;
  helpText?: string;
  value?: SelectValue;
  placeholder: string;
  loadingMessage: string;
  noOptionsMessage: string;
  creatable?: boolean;
  className?: string;
};

type State = {};

class Autocomplete extends React.Component<Props, State> {
  static defaultProps = {
    placeholder: "Type here",
    loadingMessage: "Loading...",
    noOptionsMessage: "No suggestion available",
  };

  handleInputChange = (newValue: ValueType<SelectValue>, action: ActionMeta) => {
    this.selectValue(newValue as SelectValue);
  };

  selectValue = (value: SelectValue) => {
    this.props.valueSelected(value);
  };

  // We overwrite this to avoid running into a bug (https://github.com/JedWatson/react-select/issues/2944)
  isValidNewOption = (
    inputValue: string,
    selectValue: ValueType<SelectValue>,
    selectOptions: readonly SelectValue[]
  ): boolean => {
    const isNotDuplicated = !selectOptions.map((option) => option.label).includes(inputValue);
    const isNotEmpty = inputValue !== "";
    return isNotEmpty && isNotDuplicated;
  };

  render() {
    const {
      label,
      helpText,
      value,
      placeholder,
      loadingMessage,
      noOptionsMessage,
      loadSuggestions,
      creatable,
      className,
    } = this.props;

    return (
      <div className={classNames("field", className)}>
        <LabelWithHelpIcon label={label} helpText={helpText} />
        <div className="control">
          {creatable ? (
            <AsyncCreatable
              cacheOptions
              loadOptions={loadSuggestions}
              onChange={this.handleInputChange}
              value={value}
              placeholder={placeholder}
              loadingMessage={() => loadingMessage}
              noOptionsMessage={() => noOptionsMessage}
              isValidNewOption={this.isValidNewOption}
              onCreateOption={(value) => {
                this.selectValue({
                  label: value,
                  value: {
                    id: value,
                    displayName: value,
                  },
                });
              }}
              aria-label={helpText || label}
            />
          ) : (
            <Async
              cacheOptions
              loadOptions={loadSuggestions}
              onChange={this.handleInputChange}
              value={value}
              placeholder={placeholder}
              loadingMessage={() => loadingMessage}
              noOptionsMessage={() => noOptionsMessage}
              aria-label={helpText || label}
            />
          )}
        </div>
      </div>
    );
  }
}

export default Autocomplete;
