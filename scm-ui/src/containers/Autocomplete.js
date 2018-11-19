// @flow
import React from "react";
import AsyncSelect from "react-select/lib/Async";
import { LabelWithHelpIcon } from "@scm-manager/ui-components";

export type AutocompleteObject = {
  id: string,
  displayName: string
};

type SelectValue = {
  value: AutocompleteObject,
  label: string
};

type Props = {
  loadSuggestions: string => Promise<AutocompleteObject>,
  valueSelected: AutocompleteObject => void,
  label: string,
  helpText?: string,
  value?: AutocompleteObject
};

type State = {};

class Autocomplete extends React.Component<Props, State> {
  handleInputChange = (newValue: SelectValue) => {
    this.props.valueSelected(newValue.value);
  };

  render() {
    const { label, helpText, value } = this.props;
    let selectValue = null;
    if (value) {
      selectValue = {
        value,
        label: value.displayName
      };
    }
    return (
      <div className="field">
        <LabelWithHelpIcon label={label} helpText={helpText} />
        <div className="control">
          <AsyncSelect
            cacheOptions
            loadOptions={this.props.loadSuggestions}
            onChange={this.handleInputChange}
            value={selectValue}
            placeholder="Start typing..." // TODO: i18n
            noOptionsMessage={() => <>No suggestion available</>} // TODO: i18n
          />
        </div>
      </div>
    );
  }
}

export default Autocomplete;
