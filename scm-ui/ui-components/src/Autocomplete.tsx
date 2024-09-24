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
  errorMessage?: string;
  informationMessage?: string;
  creatable?: boolean;
  className?: string;
  disabled?: boolean;
};

type State = {};

/**
 * @deprecated
 * @since 2.45.0
 *
 * Use {@link Combobox} instead
 */
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
      errorMessage,
      informationMessage,
      creatable,
      className,
      disabled,
    } = this.props;

    const asyncProps = {
      className: "autocomplete-entry",
      classNamePrefix: "autocomplete-entry",
      cacheOptions: true,
      loadOptions: loadSuggestions,
      onChange: this.handleInputChange,
      value,
      placeholder,
      loadingMessage: () => loadingMessage,
      noOptionsMessage: () => noOptionsMessage,
      isDisabled: disabled,
      "aria-label": helpText || label,
    };

    return (
      <div className={classNames("field", className)}>
        <LabelWithHelpIcon label={label} helpText={helpText} />
        <div className="control">
          {creatable ? (
            <AsyncCreatable
              {...asyncProps}
              isValidNewOption={this.isValidNewOption}
              onCreateOption={(newValue) => {
                this.selectValue({
                  label: newValue,
                  value: {
                    id: newValue,
                    displayName: newValue,
                  },
                });
              }}
            />
          ) : (
            <Async {...asyncProps} />
          )}
        </div>
        {errorMessage ? <p className="help is-danger">{errorMessage}</p> : null}
        {informationMessage ? <p className="help is-info">{informationMessage}</p> : null}
      </div>
    );
  }
}

export default Autocomplete;
