import React from "react";
import classNames from "classnames";
import { Async, AsyncCreatable } from "react-select";
import { SelectValue } from "@scm-manager/ui-types";
import LabelWithHelpIcon from "./forms/LabelWithHelpIcon";
import { ActionMeta, ValueType } from "react-select/lib/types";

type Props = {
  loadSuggestions: (p: string) => Promise<SelectValue[]>;
  valueSelected: (p: SelectValue) => void;
  label: string;
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
    noOptionsMessage: "No suggestion available"
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
    const isNotDuplicated = !selectOptions.map(option => option.label).includes(inputValue);
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
      className
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
              onCreateOption={value => {
                this.selectValue({
                  label: value,
                  value: {
                    id: value,
                    displayName: value
                  }
                });
              }}
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
            />
          )}
        </div>
      </div>
    );
  }
}

export default Autocomplete;
