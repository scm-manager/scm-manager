// @flow
import React from "react";
import AsyncSelect from "react-select/lib/Async";
import {LabelWithHelpIcon} from "@scm-manager/ui-components";

type SelectionResult = {
  id: string,
  displayName: string
};

type SelectValue = {
  value: SelectionResult,
  label: string
};

type Props = {
  loadSuggestions: string => Promise<SelectionResult>,
  valueSelected: SelectionResult => void,
  label: string,
  helpText?: string,
  value?: any
};

type State = {
  value: SelectionResult
};

class AsyncAutocomplete extends React.Component<Props, State> {
  handleInputChange = (newValue: SelectValue) => {
    this.setState({ value: newValue.value });
    this.props.valueSelected(newValue.value);
  };

  render() {
    const { label, helpText, value } = this.props;
    const stringValue = value ? value.id : "";
    return (
      <div className="field">
        <LabelWithHelpIcon label={label} helpText={helpText} />
        <div className="control">
          <AsyncSelect
            cacheOptions
            defaultOptions
            loadOptions={this.props.loadSuggestions}
            onChange={this.handleInputChange}
            value={stringValue}
          />
        </div>
      </div>
    );
  }
}

export default AsyncAutocomplete;
