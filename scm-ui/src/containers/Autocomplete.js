// @flow
import React from "react";
import { LabelWithHelpIcon } from "@scm-manager/ui-components";
import { AsyncCreatable } from "react-select";

export type AutocompleteObject = {
  id: string,
  displayName: string
};

export type SelectValue = {
  value: AutocompleteObject,
  label: string
};

type Props = {
  loadSuggestions: string => Promise<AutocompleteObject>,
  valueSelected: SelectValue => void,
  label: string,
  helpText?: string,
  value?: SelectValue
};

type State = {};

class Autocomplete extends React.Component<Props, State> {
  handleInputChange = (newValue: SelectValue) => {
    this.props.valueSelected(newValue);
  };

  isValidNewOption = (inputValue, selectValue, selectOptions) => {
    //TODO: types
    const isNotDuplicated = !selectOptions
      .map(option => option.label)
      .includes(inputValue);
    const isNotEmpty = inputValue !== "";
    return isNotEmpty && isNotDuplicated;
  };

  render() {
    const { label, helpText, value } = this.props;
    return (
      <div className="field">
        <LabelWithHelpIcon label={label} helpText={helpText} />
        <div className="control">
          <AsyncCreatable
            cacheOptions
            loadOptions={this.props.loadSuggestions}
            onChange={this.handleInputChange}
            value={value}
            placeholder="Start typing..." // TODO: i18n
            loadingMessage={() => <>Loading...</>} // TODO: i18n
            noOptionsMessage={() => <>No suggestion available</>} // TODO: i18n
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
