// @flow
import React from "react";
import { AsyncCreatable } from "react-select";
import LabelWithHelpIcon from "./LabelWithHelpIcon";
import type {AutocompleteObject, SelectValue} from "@scm-manager/ui-types";


type Props = {
  loadSuggestions: string => Promise<AutocompleteObject>,
  valueSelected: SelectValue => void,
  label: string,
  helpText?: string,
  value?: SelectValue,
  placeholder: string,
  loadingMessage: string,
  noOptionsMessage: string
};


type State = {};

class Autocomplete extends React.Component<Props, State> {


  static defaultProps = {
    placeholder: "Type here",
    loadingMessage: "Loading...",
    noOptionsMessage: "No suggestion available"
  };

  handleInputChange = (newValue: SelectValue) => {
    this.props.valueSelected(newValue);
  };

  // We overwrite this to avoid running into a bug (https://github.com/JedWatson/react-select/issues/2944)
  isValidNewOption = (inputValue: string, selectValue: SelectValue, selectOptions: SelectValue[]) => {
    const isNotDuplicated = !selectOptions
      .map(option => option.label)
      .includes(inputValue);
    const isNotEmpty = inputValue !== "";
    return isNotEmpty && isNotDuplicated;
  };

  render() {
    const { label, helpText, value, placeholder, loadingMessage, noOptionsMessage, loadSuggestions } = this.props;
    return (
      <div className="field">
        <LabelWithHelpIcon label={label} helpText={helpText} />
        <div className="control">
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
              this.handleInputChange({
                label: value,
                value: { id: value, displayName: value }
              });
            }}
          />
        </div>
      </div>
    );
  }
}


export default Autocomplete;
