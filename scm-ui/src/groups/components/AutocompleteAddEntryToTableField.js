//@flow
import React from "react";

import { AddButton } from "@scm-manager/ui-components";
import Autocomplete from "../../containers/Autocomplete";
import type {
  AutocompleteObject,
  SelectValue
} from "../../containers/Autocomplete";

type Props = {
  addEntry: SelectValue => void,
  disabled: boolean,
  buttonLabel: string,
  fieldLabel: string,
  helpText?: string,
  loadSuggestions: string => Promise<AutocompleteObject>
};

type State = {
  selectedValue?: SelectValue
};

class AutocompleteAddEntryToTableField extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { selectedValue: undefined };
  }
  render() {
    const { disabled, buttonLabel, fieldLabel, helpText, loadSuggestions } = this.props;

    const { selectedValue } = this.state;
    return (
      <div className="field">
        <Autocomplete
          label={fieldLabel}
          loadSuggestions={loadSuggestions}
          valueSelected={this.handleAddEntryChange}
          helpText={helpText}
          value={selectedValue}
        />

        <AddButton
          label={buttonLabel}
          action={this.addButtonClicked}
          disabled={disabled}
        />
      </div>
    );
  }

  addButtonClicked = (event: Event) => {
    event.preventDefault();
    this.appendEntry();
  };

  appendEntry = () => {
    const { selectedValue } = this.state;
    if (!selectedValue) {
      return;
    }
    // $FlowFixMe null is needed to clear the selection; undefined does not work
    this.setState({ ...this.state, selectedValue: null }, () =>
      this.props.addEntry(selectedValue)
    );
  };

  handleAddEntryChange = (selection: SelectValue) => {
    this.setState({
      ...this.state,
      selectedValue: selection
    });
  };
}

export default AutocompleteAddEntryToTableField;
